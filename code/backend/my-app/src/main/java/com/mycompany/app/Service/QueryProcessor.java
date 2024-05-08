package com.mycompany.app.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class QueryProcessor {
    private MongoDB mongoDB;
    private Ranker ranker;
    private PorterStemmer stemmer = new PorterStemmer();
    private static final int interval = 10;

    public QueryProcessor(MongoDB mongoDB, Ranker ranker) {
        this.mongoDB = mongoDB;
        this.ranker = ranker;
        mongoDB.initializeDatabaseConnection();
    }

    public List<Document> search(@RequestBody String query) {
        System.out.println("SEARCHING! query = " + query);
        String halfProcessedQuery = query.toLowerCase().trim();
        updateQueryHistory(halfProcessedQuery);

        // Look for phrases first
        if (query.charAt(0) == '\"') {
            String phrase;
            int i = 1;
            while (query.charAt(i) != '\"')
                i++;

            phrase = query.substring(1, i);
            return searchPhrase(phrase);
        }

        String[] queryWords = halfProcessedQuery.split("\\s+");
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
        List<Thread> threadList = new ArrayList<>();

        /* for (Map.Entry<ObjectId, Double> entry : list) {
            Document fullPageDoc = mongoDB.findPageById(entry.getKey());
            String HTMLContent = fullPageDoc.getString("HTML");
            org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);
            Double finalRank = entry.getValue() + fullPageDoc.getDouble("PageRank");
            String snippet = getSnippet(parsedDocument, stemmedQueryWords);
            // String logoURL = extractLogo(parsedDocument);
            Document resDoc = new Document("Rank", finalRank).append("URL", fullPageDoc.getString("Link"))
                    .append("Title", fullPageDoc.getString("Title")).append("Snippet", snippet);

            searchResult.add(resDoc);
        } */

        int threadNum = 0;
        for(int i = 0; i < list.size(); i = i + interval) {
            Thread thread = new searchResultWizard(threadNum, interval, stemmedQueryWords, searchResult, list);
            thread.setName("Search Wizard #" + threadNum);
            thread.start();
            threadList.add(thread);
            threadNum++;
        }

        for(Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread " + thread.getName() + " is done.");
        }

        ranker.sortPages(searchResult);
        return searchResult;
    }

    /*String getSnippet(org.jsoup.nodes.Document parsedDocument, String[] queryWords) {
        Elements elements = parsedDocument.select("p");

        for (Element element : elements) {
            String text = element.ownText();
            String lowerCaseText = text.toLowerCase();

            for (String queryWord : queryWords) {
                if (ProcessingWords.isStopWord(queryWord))
                    continue;

                if (lowerCaseText.contains(queryWord)) {
                    // return text;
                    return makeSnippetWordsBold(text, queryWords);
                }
            }
        }
        return "";
    }*/

    private String makeSnippetWordsBold(String snippet, String[] queryWordsArray) {
        String[] snippetWords = snippet.split("\\s+");
        List<String> queryWords = new ArrayList<>();

        for (String word : queryWordsArray) {
            if (!ProcessingWords.isStopWord(word))
                queryWords.add(word);
        }

        StringBuilder snippetBuilder = new StringBuilder();
        boolean makeBold;

        for (String snippetWord : snippetWords) {
            makeBold = false;
            for (String queryWord : queryWords) { 
                if (snippetWord.equalsIgnoreCase(queryWord) || snippetWord.contains(queryWord)) {
                    makeBold = true;
                    break;
                }
            }
            if (makeBold)
                snippetBuilder.append("<b> ");
            snippetBuilder.append(snippetWord).append(" ");
            if(makeBold)
                snippetBuilder.append("</b> ");
        }
        return snippetBuilder.toString().trim();
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
        System.out.println("GEtting suggesstions query: " + query);
        MongoCursor<Document> cursor = mongoDB.getQueryHistory();
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
        Set<ObjectId> foundPages = mongoDB.searchPhrase(query);
        List<Document> searchResult = new ArrayList<>();

        for (ObjectId pageID : foundPages) {
            Document page = mongoDB.findPageById(pageID);
            String HTMLContent = page.getString("HTML");
            org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);
            // String logoURL = extractLogo(parsedDocument);
            Elements elements = parsedDocument.select("p");

            for (Element element : elements) {

                String text = element.ownText();
                String lowerCaseText = text.toLowerCase();
                if (lowerCaseText.contains(query)) {
                    searchResult
                            .add(new Document("URL", page.getString("Link")).append("Title", page.getString("Title"))
                                    .append("Snippet", makePhraseSnippetBold(text, query)).append("Rank", page.getDouble("PageRank")));
                    break;
                }
            }
        }
        ranker.sortPages(searchResult);
        return searchResult;
    }

    public String makePhraseSnippetBold(String snippet, String query) {
        StringBuilder snippetBuilder = new StringBuilder();
        for (int i = 0; i + query.length() < snippet.length(); i++) {
            if(snippet.substring(i, i + query.length()).equalsIgnoreCase(query)) {
                snippetBuilder.append(snippet.substring(0, i));
                snippetBuilder.append(" <b> ");
                snippetBuilder.append(snippet.substring(i, i + query.length()));
                snippetBuilder.append(" </b> ");
                snippetBuilder.append(snippet.substring(i + query.length(), snippet.length())); 
            }
        }
        return snippetBuilder.toString();
    }

    class searchResultWizard extends Thread {
        private int num;
        private int interval;
        List<Map.Entry<ObjectId, Double>> pageList;
        String[] stemmedQueryWords;
        List<Document> searchResult;

        public searchResultWizard(int num, int interval, String[] stemmedQueryWords, List<Document> searchResult, 
        List<Map.Entry<ObjectId, Double>> pageList) {
            this.num = num;
            this.interval = interval;
            this.stemmedQueryWords = stemmedQueryWords;
            this.searchResult = searchResult;
            this.pageList = pageList;
        }

        public void run() {
            for (int i = num * interval; i < pageList.size() && i < ((num + 1) * interval); i++) {
                Document fullPageDoc;
                synchronized(mongoDB) {
                    fullPageDoc = mongoDB.findPageById(pageList.get(i).getKey());
                }
                String HTMLContent = fullPageDoc.getString("HTML");
                org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);
                Double finalRank = pageList.get(i).getValue() + fullPageDoc.getDouble("PageRank");
                String snippet = getSnippet(parsedDocument, stemmedQueryWords);
                // logo
                Document resDoc = new Document("Rank", finalRank).append("URL", fullPageDoc.getString("Link"))
                    .append("Title", fullPageDoc.getString("Title")).append("Snippet", snippet);

                synchronized(searchResult) {
                    searchResult.add(resDoc);
                }
            } 
        }    

        String getSnippet(org.jsoup.nodes.Document parsedDocument, String[] queryWords) {
            Elements elements = parsedDocument.select("p");
    
            for (Element element : elements) {
                String text = element.ownText();
                String lowerCaseText = text.toLowerCase();
    
                for (String queryWord : queryWords) {
                    if (ProcessingWords.isStopWord(queryWord))
                        continue;
    
                    if (lowerCaseText.contains(queryWord)) {
                        // return text;
                        return makeSnippetWordsBold(text, queryWords);
                    }
                }
            }
            return "";
        }
    }
}
