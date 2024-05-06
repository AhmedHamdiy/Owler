package com.mycompany.app.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.mongodb.client.MongoCursor;
import com.mycompany.app.MongoDB;
import com.mycompany.app.Ranker.Ranker;

import opennlp.tools.stemmer.PorterStemmer;

@Service
public class QueryProcessorService {
    private MongoDB mongoDB;
    private Ranker ranker;
    private PorterStemmer stemmer = new PorterStemmer();

    public QueryProcessorService(MongoDB mongoDB, Ranker ranker) {
        this.mongoDB = mongoDB;
        this.ranker = ranker;
        mongoDB.initializeDatabaseConnection();
    }

    public List<Document> search(@RequestBody String query) {
        String processedQuery = query.toLowerCase().trim();
        updateQueryHistory(processedQuery);

        // Look for phrase searching first
        if (query.charAt(0) == '\"') {
            String phrase;
            int i = 1;
            while (query.charAt(i) != '\"')
                i++;

            phrase = query.substring(1, i - 1);
            return searchPhrase(phrase);
        }

        String[] queryWords = processedQuery.split("\\s+");
        String[] stemmedQueryWords = Arrays.copyOf(queryWords, queryWords.length);
        HashMap<ObjectId, Double> map = new HashMap<ObjectId, Double>();

        for (int i = 0; i < queryWords.length; i++) {
            stemmedQueryWords[i] = stemmer.stem(queryWords[i]);
        }

        for (String stemmedQueryWord : stemmedQueryWords) {
            // use original word queryWord
            if (ProcessingWords.isStopWord(stemmedQueryWord))
                continue;

            MongoCursor<Document> cursor = mongoDB.getWordPagesCursor(stemmedQueryWord);

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object obj = doc.get("Pages");

                for (Document page : (List<Document>) obj) {
                    ObjectId id = page.getObjectId("_id");
                    Double TF_IDF = page.getDouble("TF_IDF");
                    Double tagNum = page.getDouble("Tag");
                    Double relevance = TF_IDF + tagNum;

                    Double prev = map.get(id);
                    if (prev == null)
                        map.put(id, relevance);
                    else
                        map.put(id, relevance + prev);
                }
            }
        }

        List<Map.Entry<ObjectId, Double>> list = new ArrayList<>(map.entrySet());
        List<Document> searchResult = new ArrayList<>();

        for (Map.Entry<ObjectId, Double> entry : list) {
            Document fullPageDoc = mongoDB.findPageById(entry.getKey());
            String HTMLContent = fullPageDoc.getString("HTML");
            org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);
            Double finalRank = entry.getValue() + fullPageDoc.getDouble("PageRank");
            String snippet = getSnippet(parsedDocument, stemmedQueryWords);
            snippet = makeSnippetWordsBold(snippet, stemmedQueryWords);
            String logoURL = extractLogo(parsedDocument);
            Document resDoc = new Document("Rank", finalRank).append("URL", fullPageDoc.getString("Link"))
                    .append("Title", fullPageDoc.getString("Title")).append("Logo", logoURL)
                    .append("Snippet", snippet);

            searchResult.add(resDoc);
        }

        ranker.sortPages(searchResult);
        return searchResult;
    }

    String getSnippet(org.jsoup.nodes.Document parsedDocument, String[] queryWords) {
        // String HTMLContent = page.getString("HTML");
        // org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);
        Elements elements = parsedDocument.select("*");
        StringBuilder snippetBuilder = new StringBuilder();
        int headerCount = 0;
        int paragraphCount = 0;
        int otherElementCount = 0;

        for (Element element : elements) {

            String tagName = element.tagName();
            if (tagName.equals("a") || tagName.equals("img")
                    || tagName.equals("br") || tagName.equals("hr")
                    || tagName.equals("input") || tagName.equals("button")
                    || tagName.equals("h1"))
                continue;

            String text = element.ownText();
            String lowerCaseText = text.toLowerCase();

            for (String queryWord : queryWords) {

                if (ProcessingWords.isStopWord(queryWord))
                    continue;

                if (lowerCaseText.contains(queryWord)) {
                    // perform checks
                    if (tagName.contains("h") && headerCount > 2)
                        continue;
                    else if (tagName.equals("p") && paragraphCount > 0)
                        continue;
                    else if (!tagName.contains("h") && !tagName.equals("p") && (otherElementCount > 1 || paragraphCount > 0))
                        continue;

                    snippetBuilder.append(text);
                    snippetBuilder.append(" <br> ");

                    if (tagName.contains("h"))
                        headerCount++;
                    else if (tagName.equals("p"))
                        paragraphCount++;
                    else
                        otherElementCount++;
                }
            }
        }
        return snippetBuilder.toString().trim();
    }

    private String makeSnippetWordsBold(String snippet, String[] queryWords) {
        String[] snippetWords = snippet.split("\\s+");
        List<Integer> indicesToInsertAt = new ArrayList<>();
        String resultSnippet;

        for (String queryWord : queryWords) {
            if (ProcessingWords.isStopWord(queryWord))
                continue;

            for (int i = 0; i < snippetWords.length; i++) {
                if (snippetWords[i].equalsIgnoreCase(queryWord)) {
                    indicesToInsertAt.add(i);
                }
            }
        }

        indicesToInsertAt.sort(null);
        Collections.sort(indicesToInsertAt);

        if (!indicesToInsertAt.isEmpty()) {
            StringBuilder snippetBuilder = new StringBuilder();
            
            int j = 0;
            for (int i = 0; i < snippetWords.length && j < indicesToInsertAt.size(); i++) {
                int index = indicesToInsertAt.get(j);
                
                if (i == index)
                    snippetBuilder.append(" <b> ");

                snippetBuilder.append(snippetWords[i]).append(" ");
                
                if (i == index) {
                    snippetBuilder.append(" </b> ");
                    j++;
                }
            }
            resultSnippet = snippetBuilder.toString();
        } else 
            return snippet;

        return resultSnippet;
    }

    private String extractLogo(org.jsoup.nodes.Document page) {
        Elements imgTags = page.select("img");
        for (Element img : imgTags) {
            String imgUrl = img.attr("src");

            if (isLikelyLogo(imgUrl))
                return imgUrl;
        }
        return null;
    }

    private static boolean isLikelyLogo(String url) {
        // might add more sophisticated logic here
        return url.contains("logo") || url.contains("brand");
    }

    private static String extractText(Element element) {
        StringBuilder text = new StringBuilder();
        for (Node node : element.childNodes()) {
            if (node instanceof Element) {
                Element childElement = (Element) node;
                if ("a".equalsIgnoreCase(childElement.tagName())) {
                    // Handle <a> elements separately to include their text content in the correct
                    // position
                    // text.append(" ").append(childElement.text()).append(" ");
                    text.append(childElement.text());
                }
            } else {
                // Append text content of non-element nodes
                text.append(node.outerHtml());
            }
        }
        return text.toString().trim();
    }

    void updateQueryHistory(String query) {
        if (mongoDB.updateQueryHistory(query) == 0) {
            Document newQueryHistory = new Document("Query", query).append("Popularity", 1);
            mongoDB.insertOne(newQueryHistory, "QueryHistory");
        }
    }

    public List<String> getSuggestions(String query) {
        MongoCursor<Document> cursor = mongoDB.getQueryHistoryCursor();
        List<Document> suggestionDocs = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        query = query.trim();

        while (cursor.hasNext()) {
            Document prevQueryDoc = cursor.next();
            String prevQuery = prevQueryDoc.getString("Query");

            if (prevQuery.startsWith(query))
                suggestionDocs.add(prevQueryDoc);
        }

        suggestionDocs.sort(new Comparator<Document>() {
            @Override
            public int compare(Document d1, Document d2) {
                return d2.getInteger("Popularity").compareTo(d1.getInteger("Popularity"));
            }
        });

        for (Document document : suggestionDocs) {
            suggestions.add(document.getString("Query"));
        }

        return suggestions;
    }

    public List<Document> searchPhrase(String query) {
        System.out.println("Searching for phrase");
        query = query.toLowerCase();
        List<Document> crawledPages = mongoDB.getCrawlerPages();
        List<Document> searchResult = new ArrayList<>();
        updateQueryHistory(query.trim());

        for (Document page : crawledPages) {
            String HTMLContent = page.getString("HTML");
            org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);
            Elements elements = parsedDocument.select("*");
            String logoURL = extractLogo(parsedDocument);

            for (Element element : elements) {

                String tagName = element.tagName();
                if (tagName.equals("a") || tagName.equals("img")
                        || tagName.equals("br") || tagName.equals("hr")
                        || tagName.equals("input") || tagName.equals("button"))
                    continue;

                String text = element.ownText();
                String lowerCaseText = text.toLowerCase();
                if (lowerCaseText.contains(query)) {
                    searchResult
                            .add(new Document("URL", page.getString("Link")).append("Title", page.getString("Title"))
                                    .append("Logo", logoURL).append("Snippet", text));

                    break;
                }
            }
        }
        return searchResult;
    }
}
