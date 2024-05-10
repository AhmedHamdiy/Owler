package com.mycompany.app.Ranker;

import java.util.*;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mycompany.app.MongoDB;

@Component
public class Ranker {
    final private MongoDB mongoDB = new MongoDB();
    final private String pageCollection = "Page";
    final private String wordCollection = "Word";
    final private String historyCollection = "History";
    final private String retrievedCollection = "Retrieved";

    public Ranker() {
        mongoDB.initializeDatabaseConnection();
    }

    public void sortPages(List<Document> unsortedResult) {
        unsortedResult.sort(new Comparator<Document>() {
            @Override
            public int compare(Document d1, Document d2) {
                return d2.getDouble("Rank").compareTo(d1.getDouble("Rank"));
            }
        });
    }

    void computePagePopularity() {
        long numPages = mongoDB.pageCollection.countDocuments();
        System.out.println("Number of pages in db: " + numPages);

        double dampingFactor = 0.85;
        Graph PRGraph = createGraph(numPages);
        connectNodes(PRGraph);

        boolean updated = true;
        while (updated) {

            // resetting 'updated' at start of each iteration
            updated = false;

            for (Graph.Node node : PRGraph.nodes) {
                double prevPageRank = node.pageRank;
                double newPageRank = (1 - dampingFactor) / (double) numPages;
                // calculate sum of share of referencing pages' pageranks
                double sigma = 0;

                for (Graph.Node ref : node.refs) {
                    sigma += ref.pageRank / (double) ref.numOutgoingLinks;
                }

                newPageRank += dampingFactor * sigma;
                node.pageRank = newPageRank;

                // If page rank values have not yet converged, then we go for another iteration
                if (newPageRank != prevPageRank)
                    updated = true;
            }
        }

        for (Graph.Node node : PRGraph.nodes)
            mongoDB.pageCollection.updateOne(Filters.eq("Link", node.URL), Updates.set("PageRank", node.pageRank));
    }

    /**
     * Creates a new graph and creates a node for each page in the DB collection,
     * and stores its URL, initial page rank and array of outgoing links
     * 
     * @param numPages
     */
    Graph createGraph(long numPages) {
        MongoCursor<Document> cursor = mongoDB.pageCollection.find().iterator();
        Graph PRGraph = new Graph();
        double initialPR = (double) 1 / numPages;

        // Creating a node in the graph for each page, and storing its URL, page rank
        // and outgoing URLs
        while (cursor.hasNext()) {
            Document page = cursor.next();
            String URL = page.getString("Link");

            String HTMLContent = page.getString("HTML");
            org.jsoup.nodes.Document parsedPage = Jsoup.parse(HTMLContent);
            Elements aTags = parsedPage.select("a[href]");

            Graph.Node newNode = new Graph.Node(URL, initialPR);

            // Fill array of outgoing links
            for (Element tag : aTags) {
                String outgoingURL = tag.attr("href");
                newNode.outgoingLinks.add(outgoingURL);
            }
            PRGraph.nodes.add(newNode);
        }
        return PRGraph;
    }

    /**
     * This function builds the connections inside the graph of pages.
     * It iterates over each node in the graph, and checks out its outgoing links,
     * if a link leads to a page that exists in the graph, it stores a ref in the
     * *referenced* page,
     * if the page does not exist in the graph, it deletes the URL from the array
     * outgoingLinks.
     * 
     * @param PRGraph
     */
    void connectNodes(Graph PRGraph) {
        for (Graph.Node node : PRGraph.nodes) {
            Iterator<String> it = node.outgoingLinks.iterator();

            while (it.hasNext()) {
                String link = it.next();
                Graph.Node referencedPage;

                if ((referencedPage = PRGraph.containsNode(link)) != null) {
                    referencedPage.refs.add(node);
                } else {
                    it.remove();
                }
            }
        }

        for (Graph.Node node : PRGraph.nodes) {
            node.numOutgoingLinks = node.outgoingLinks.size();
        }
    }

    public static void main(String[] args) {
        Ranker rank = new Ranker();
        rank.computePagePopularity();
    }
}
