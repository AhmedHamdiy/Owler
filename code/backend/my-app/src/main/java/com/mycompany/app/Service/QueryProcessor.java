package com.mycompany.app.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

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

    public List<Document> search(String query) {
        String halfProcessedQuery = query.toLowerCase().trim();
        updateQueryHistory(halfProcessedQuery);

        // Look for phrases first
        if (query.charAt(0) == '\"') 
            return readLogicalExpression(halfProcessedQuery);

        String[] queryWords = halfProcessedQuery.split("\\s+");
        String[] stemmedQueryWords = Arrays.copyOf(queryWords, queryWords.length);
        HashMap<ObjectId, Double> resultPageMap = new HashMap<>();
        HashMap<ObjectId, Boolean> matchMap = new HashMap<>();

        for (int i = 0; i < queryWords.length; i++) {
            stemmedQueryWords[i] = stemmer.stem(queryWords[i]);
        }

        String stemmedWord, originalWord;
        for (int i = 0; i < stemmedQueryWords.length; i++) {
            stemmedWord = stemmedQueryWords[i];
            originalWord = queryWords[i];

            if (ProcessingWords.isStopWord(stemmedWord))
                continue;

            MongoCursor<Document> cursor = mongoDB.getPagesInfoPerWord(stemmedWord);

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object obj = doc.get("Pages");
                String pageOriginalWord = doc.getString("Word");

                for (Document page : (List<Document>) obj) {
                    ObjectId id = page.getObjectId("_id");
                    Double TF_IDF = page.getDouble("TF_IDF");
                    Double tagNum = page.getDouble("Tag");
                    Double relevance = TF_IDF + tagNum;

                    Double prev = resultPageMap.get(id);
                    if (prev == null) {
                        resultPageMap.put(id, relevance);

                        if (pageOriginalWord.equalsIgnoreCase(originalWord))
                            matchMap.put(id, true);
                        else
                            matchMap.put(id, false);
                    }
                    else {
                        resultPageMap.put(id, relevance + prev);

                        if (pageOriginalWord.equalsIgnoreCase(originalWord))
                            matchMap.put(id, true);
                    }
                }
            }
        }

        List<Map.Entry<ObjectId, Double>> list = new ArrayList<>(resultPageMap.entrySet());
        List<Document> searchResult = new ArrayList<>();
        List<Document> secondaryResult = new ArrayList<>();
        List<Thread> threadList = new ArrayList<>();
        int threadNum = 0;

        for (int i = 0; i < list.size(); i = i + interval) {
            Thread thread = new searchResultWizard(threadNum, stemmedQueryWords, searchResult, secondaryResult, list, matchMap);
            thread.setName("Search Wizard #" + threadNum);
            thread.start();
            threadList.add(thread);
            threadNum++;
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread " + thread.getName() + " is done.");
        }

        searchResult.addAll(secondaryResult);
        ranker.sortPages(searchResult);
        return searchResult;
    }

    public List<String> getSuggestions(String query) {
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

    private void updateQueryHistory(String query) {
        if (mongoDB.updateQueryHistory(query) == 0) {
            Document newQueryHistory = new Document("Query", query).append("Popularity", 1);
            mongoDB.insertOne(newQueryHistory, "QueryHistory");
        }
    }

    private List<Document> searchByPhrase(String query, Set<ObjectId> resultPagesIndex) {
        Set<ObjectId> foundPages = mongoDB.searchByWords(query);
        List<Document> searchResult = new ArrayList<>();
        query = " " + query + " ";

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
                                    .append("Snippet", makePhraseSnippetBold(text, query))
                                    .append("Rank", page.getDouble("PageRank")).append("_id", pageID));
                    resultPagesIndex.add(pageID);
                    break;
                }
            }
        }
        ranker.sortPages(searchResult);
        return searchResult;
    }

    private String makePhraseSnippetBold(String snippet, String query) {
        StringBuilder snippetBuilder = new StringBuilder();
        for (int i = 0; i + query.length() < snippet.length(); i++) {
            if (snippet.substring(i, i + query.length()).equalsIgnoreCase(query)) {
                snippetBuilder.append(snippet.substring(0, i)).append(" <b>");
                snippetBuilder.append(snippet.substring(i, i + query.length())).append("</b> ");
                snippetBuilder.append(snippet.substring(i + query.length(), snippet.length()));
            }
        }
        return snippetBuilder.toString();
    }

    private List<Document> readLogicalExpression(String query) {
        int i = 0;
        char next;
        Operation first = Operation.NULL;
        Operation second = Operation.NULL;
        List<String> phraseList = new ArrayList<>();

        while (i < query.length()) {
            next = query.charAt(i);

            if (next == '\"') {
                int phraseLength = readPhrase(phraseList, query, i);
                i += phraseLength + 1;
            } else if (next == 'a') {
                if (first == Operation.NULL)
                    first = Operation.AND;
                else
                    second = Operation.AND;
                i += 3;
            } else if (next == 'o') {
                if (first == Operation.NULL)
                    first = Operation.OR;
                else
                    second = Operation.OR;
                i += 2;
            } else if (next == 'n') {
                if (first == Operation.NULL)
                    first = Operation.NOT;
                else
                    second = Operation.NOT;
                i += 3;
            }
            i++;
        }

        return executeLogicalExpression(phraseList, first, second);
    }

    private List<Document> executeLogicalExpression(List<String> phraseList, Operation first, Operation second) {
        Set<ObjectId> resultPagesIndex = new HashSet<>();
        if (phraseList.size() == 1) {
            return searchByPhrase(phraseList.get(0), resultPagesIndex);
        }
        List<Document> searchResult = new ArrayList<>();

        if (first == Operation.OR && second == Operation.AND) {
            first = Operation.AND;
            second = Operation.OR;
            String firstPhrase = phraseList.remove(0);
            String secondPhrase = phraseList.remove(0);
            phraseList.add(secondPhrase);
            phraseList.add(firstPhrase);
        }

        switch (first) {
            case Operation.AND:
                searchResult = executeAND(phraseList.get(0), phraseList.get(1), resultPagesIndex);
                break;
            case Operation.OR:
                searchResult = executeOR(phraseList.get(0), phraseList.get(1), resultPagesIndex);
                break;
            case Operation.NOT:
                searchResult = searchByPhrase(phraseList.get(0), resultPagesIndex);
                filterResult(searchResult, phraseList.get(1));
                break;
            default:
                break;
        }

        switch (second) {
            case Operation.AND:
                Set<ObjectId> set = new HashSet<>();
                List<Document> res1 = searchByPhrase(phraseList.get(2), set);
                resultPagesIndex.retainAll(set);
                List<Document> prevRes = new ArrayList<>(searchResult);
                prevRes.addAll(res1);
                searchResult.clear();

                for (ObjectId pageID : resultPagesIndex) {
                    for (Document resultPage : prevRes) {
                        if (resultPage.getObjectId("_id") == pageID && !containsDocument(pageID, searchResult))
                            searchResult.add(resultPage);
                    }
                }
                break;
            case Operation.OR:
                List<Document> res2 = searchByPhrase(phraseList.get(2), resultPagesIndex);

                for (ObjectId pageID : resultPagesIndex) {
                    for (Document resultPage : res2) {
                        if (resultPage.getObjectId("_id") == pageID && !containsDocument(pageID, searchResult))
                            searchResult.add(resultPage);
                    }
                }
                break;
            case Operation.NOT:
                filterResult(searchResult, phraseList.get(2));
            default:
                break;
        }
        return new ArrayList<>(searchResult);
    }

    private void filterResult(List<Document> searchResult, String notExpression) {
        Iterator<Document> iterator = searchResult.iterator();

        while (iterator.hasNext()) {
            Document doc = iterator.next();

            Document fullPageDoc = mongoDB.findPageById(doc.getObjectId("_id"));
            String HTMLContent = fullPageDoc.getString("HTML");
            org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);

            if (parsedDocument.text().contains(notExpression))
                iterator.remove();
        }
    }

    private enum Operation {
        AND,
        OR,
        NOT,
        NULL
    }

    private boolean containsDocument(ObjectId id, List<Document> docList) {
        for (Document doc : docList) 
            if (doc.getObjectId(docList) == id)
                return true;
        return false;
    }
   
    private List<Document> executeOR(String phrase1, String phrase2, Set<ObjectId> resultsPageIndex) {
        List<Document> result1 = searchByPhrase(phrase1, resultsPageIndex);
        List<Document> result2 = searchByPhrase(phrase2, resultsPageIndex);

        result1.addAll(result2);
        List<Document> finalResult = new ArrayList<>();

        for (ObjectId pageID : resultsPageIndex) {
            for (Document resultPage : result1) {
                if (resultPage.getObjectId("_id") == pageID)
                    finalResult.add(resultPage);
            }
        }
        return finalResult;
    }

    private List<Document> executeAND(String phrase1, String phrase2, Set<ObjectId> resultsPageIndex) {
        Set<ObjectId> pageIdSet = new HashSet<>();
        List<Document> result1 = searchByPhrase(phrase1, resultsPageIndex);
        List<Document> result2 = searchByPhrase(phrase2, pageIdSet);

        resultsPageIndex.retainAll(pageIdSet);
        result1.addAll(result2);
        List<Document> finalResult = new ArrayList<>();

        for (ObjectId pageID : resultsPageIndex) {
            for (Document resultPage : result1) {
                if (resultPage.getObjectId("_id") == pageID)
                    finalResult.add(resultPage);
            }
        }
        return finalResult;
    }
    
    private int readPhrase(List<String> phraseList, String query, int i) {
        i++;
        int j = i;

        while (query.charAt(i) != '\"')
            i++;

        String phrase = query.substring(j, i);
        phraseList.add(phrase);
        return phrase.length();
    }

    class searchResultWizard extends Thread {
        private int num;
        List<Map.Entry<ObjectId, Double>> pageList;
        String[] stemmedQueryWords;
        List<Document> searchResult;
        List<Document> secondaryResult;
        Map<ObjectId, Boolean> matchMap;

        public searchResultWizard(int num, String[] stemmedQueryWords, List<Document> searchResult, List<Document> secondaryResult,
                List<Map.Entry<ObjectId, Double>> pageList, Map<ObjectId, Boolean> matchMap) {
            this.num = num;
            this.stemmedQueryWords = stemmedQueryWords;
            this.searchResult = searchResult;
            this.secondaryResult = secondaryResult;
            this.pageList = pageList;
            this.matchMap = matchMap;
        }

        public void run() {
            for (int i = num * interval; i < pageList.size() && i < ((num + 1) * interval); i++) {
                Document fullPageDoc;
                ObjectId id = pageList.get(i).getKey();
                synchronized (mongoDB) {
                    fullPageDoc = mongoDB.findPageById(id);
                }
                String HTMLContent = fullPageDoc.getString("HTML");
                org.jsoup.nodes.Document parsedDocument = Jsoup.parse(HTMLContent);
                Double finalRank = pageList.get(i).getValue() + fullPageDoc.getDouble("PageRank");
                String snippet = getSnippet(parsedDocument, stemmedQueryWords);
                if (snippet.length() == 0)
                    continue;
                // logo
                Document resDoc = new Document("Rank", finalRank).append("URL", fullPageDoc.getString("Link"))
                        .append("Title", fullPageDoc.getString("Title")).append("Snippet", snippet);


                if (matchMap.get(id)) {
                    synchronized (searchResult) {
                        searchResult.add(resDoc);
                    }
                } else {
                    synchronized (secondaryResult) {
                        secondaryResult.add(resDoc);
                    }
                }
            }
        }

        private String getSnippet(org.jsoup.nodes.Document parsedDocument, String[] queryWords) {
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
                if (makeBold)
                    snippetBuilder.append("</b> ");
            }
            return snippetBuilder.toString().trim();
        }
    }
}
