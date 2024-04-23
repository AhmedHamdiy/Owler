package com.mycompany.app;

import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.ObjID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.Math;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.apache.lucene.index.IndexWriter;
import org.bson.types.ObjectId;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.client.model.Updates;

// import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class Indexer {

    static MongoDB mongo;

    static class Pair {
        private int first;
        private String second;

        public Pair(int first, String second) {
            this.first = first;
            this.second = second;
        }

        public void setFirst(int fir) {
            first = fir;
        }

        public void setSecond(String sec) {
            second = sec;
        }

        public int getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return "[" + first + ", " + second + "]";
        }
    }

    static int Quantim = -1;

    static String suffixPattern = "(ly|ward|wise|ed|en|er|ing|ize|iseable|ible|al|ant|ary|fulious|ous|ive|less|eer|er|ion|ism|ity|ment|ness|or|sion|ship|th|ful)$";

    public static void main(String[] args) throws Exception {

        // ================make the monogo connection================//
        mongo = new MongoDB();
        mongo.initializeDatabaseConnection();

        long startTime = System.currentTimeMillis();

        System.out.println("Enter Number of page per thread : ");

        while (Quantim < 1)
            try {
                Quantim = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
            } catch (Exception e) {
                System.out.println("Please Enter a Number");
                Quantim = 0;
            }

        List<Document> PageDocument = mongo.getCrawllerPages();

        // for (Document d : PageDocument) {
        //     Object ID = d.get("_id");
        //     ObjectId id = (ObjectId) ID;
        //     mongo.isIndexed(id);

        // }
        List<Thread> arrThread = new ArrayList<>();
        int numThread = 0;
        // Thread t = new index(0, PageDocument.size(), PageDocument);
        // t.start();
        // t.join();
        for (int i = 0; i < PageDocument.size(); i = i + Quantim) {
        Thread t = new index(numThread, Quantim, PageDocument);
        numThread++;
        t.setName("Indexer Spider " + numThread);
        t.start();
        arrThread.add(t);

        }
        // new index(0, URLS.length, URLS).start();

        for (Thread tt : arrThread) {
        tt.join();
        System.out.println("the Thread " + tt.getName() + " is killed");
        }

        List<Document> WordDocument = mongo.getWords();
        List<Thread> arrThreads = new ArrayList<>();
        numThread = 0;
        // Thread te = new UpdateIDF(0, WordDocument.size(), WordDocument);
        // te.start();
        // te.join();

        for (int i = 0; i < WordDocument.size(); i = i + 1000) {
        Thread t = new UpdateIDF(numThread, 1000, WordDocument);
        numThread++;
        t.setName("Indexer Spider IDF " + numThread);
        t.start();
        arrThreads.add(t);

        }
        for (Thread tt : arrThreads) {
        tt.join();
        System.out.println("the Thread IDF " + tt.getName() + " is killed");
        }

        mongo.closeConnection();

        long finishTime = System.currentTimeMillis();
        System.out.println("Time taken to indexer :" + (finishTime - startTime) + "ms");

    }

    private static boolean isStopWord(String word) {

        Path path = Paths.get(
                "D:\\term2_sec\\APT\\Project\\My-Part\\Crowler\\code\\backend\\my-app\\src\\main\\java\\com\\mycompany\\app\\stopWords.txt");
        try {
            String content = Files.readString(path);
            String[] stopWords = content.split(",");

            for (String stopWord : stopWords) {
                if (stopWord.trim().equalsIgnoreCase(word)) {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    static class index extends Thread {
        int num;// the number of the theread
        int quntum;
        List<Document> pagesDocument;

        public index(int n, int q, List<Document> d) {
            num = n;
            quntum = q;
            pagesDocument = d;
        }

        public void run() {

            for (int i = num * quntum; i < pagesDocument.size() && i < ((num + 1) * quntum); i++) {
                String htmlString = (String) pagesDocument.get(i).get("HTML");
                org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
                // org.jsoup.nodes.Document document = getreq(pagesDocument.get(i));
                // Object obj = pagesDocument.get(i).get("HTML");
                // org.jsoup.nodes.Document document = (org.jsoup.nodes.Document) obj;

                /// ============= if the page is indexed ignor it =============///

                Object isIndexed = pagesDocument.get(i).get("isIndexed");
                boolean is_indexed = (boolean) isIndexed;

                if (is_indexed)
                    continue;

                // =====make filtered words for the words=============//

                Map<String, Pair> wordFreq = new HashMap<>();
                double numTerms = 0;
                // ==========get the document elemenents=============//
                if (document == null)
                    continue;

                Elements elements = document.select("*");

                // =========loop over the array of elements=========//

                for (Element element : elements) {
                    String tagname = element.tagName();
                    // ============ ignore the words in some tags===================//
                    if (tagname.equals("a") || tagname.equals("img") || tagname.equals("br") || tagname.equals("hr")
                            || tagname.equals("input") || tagname.equals("button"))
                        continue;

                    // =============remove numbers==================//

                    String text = element.ownText().trim().replaceAll("\\b\\d+\\b", "");
                    // ============== remove special characters=========//

                    text = text.replaceAll("[^a-zA-Z\\s]", "");
                    if (!text.isEmpty()) {
                        // ========= split the text to words==========///

                        String[] words = text.split("\\s+");

                        /// ============== loop over the word and remove the stope words===========//
                        for (String word : words) {
                            word = word.replaceAll(suffixPattern, "");
                            if (isStopWord(word) || word == "" || word.length() == 1 || word.length() == 2) {
                                continue;
                            }
                            numTerms++;
                            if (wordFreq.containsKey(word)) {
                                int frequency = wordFreq.get(word).first;

                                wordFreq.put(word, new Pair(frequency, tagname));
                            } else {
                                wordFreq.put(word, new Pair(1, tagname));
                            }

                        }
                    }
                }

                // =========get the page id ======================//
                Object ID = pagesDocument.get(i).get("_id");
                ObjectId id = (ObjectId) ID;

                int freq; // ===>FREQUENCY OF THE WORD
                double TF; // ===>TF OF THE PAGE

                for (Map.Entry<String, Pair> word : wordFreq.entrySet()) {

                    freq = (int) word.getValue().first;
                    TF = freq / numTerms;

                    // int type = processElement(document, (String) word.getKey());

                    Document newPage = new Document("_id", id).append("frequency",
                            freq).append("TF", TF).append("tag", (String) word.getValue().second);

                    // see if this word is header or paragraph in the document
                    synchronized (mongo) {
                        if (!mongo.isContainWord((String) word.getKey())) {
                            // intialize the array of pages
                            List<Document> pageList = new ArrayList<>();
                            // add the current page to it
                            pageList.add(newPage);
                            // initialize a new Document word
                            Document newWord = new Document("_id", new ObjectId()).append("word",
                                    word.getKey()).append("pages",
                                            pageList);
                            // insert word into database
                            String collecString = "Word";

                            mongo.insertOne(newWord, collecString);

                        } else {
                            // update the document with the list of pages
                            List<Document> iterable = mongo.FindWordPages((String) word.getKey());
                            iterable.add(newPage);
                            // set the new list of pages
                            mongo.updatePagesList((String) word.getKey(), iterable);
                            // }

                        }
                    }

                    System.err.println();
                    System.err.println();
                    System.err.println("DONE");
                    System.err.println();
                }

                mongo.isIndexed(id);
            }
            // }
            System.err.println();
            System.err.println();
            System.err.println("I am here now so i can updata IDF");

            // IDFupdate();
            System.err.println();
            System.err.println();
            System.err.println("DONE  all");
            System.err.println();
            // mongo.closeConnection();

        }

    }

    static class UpdateIDF extends Thread {
        int num;// the number of the theread
        int quntum;
        List<Document> WordDocument;

        public UpdateIDF(int n, int q, List<Document> d) {
            num = n;
            quntum = q;
            WordDocument = d;
        }

        public void run() {
            double IDF;
            List<Document> pagesList = new ArrayList<>();

            // loop over the list of word document

            for (int i = num * quntum; i < WordDocument.size() && i < ((num + 1) * quntum); i++) {
                // git list of pages of the word
                List<Document> pages = new ArrayList<>();

                // Check if the "pages" field exists and is of the correct type
                if (WordDocument.get(i).containsKey("pages")) {

                    Object pagesObject = WordDocument.get(i).get("pages");
                    if (pagesObject instanceof List) {
                        List<?> pagesLists = (List<?>) pagesObject;
                        for (Object page : pagesLists) {
                            if (page instanceof Document)
                                pages.add((Document) page);

                        }

                        // num of pages that have that word
                        double numOfPagePerWord = pages.size();

                        // calculate the IDF
                        IDF = Math.log10(6000 / numOfPagePerWord);
                        /// loop over the pages

                        pagesList = new ArrayList<>();
                        for (Document page : pages) {

                            double rank;
                            double tf = page.getDouble("TF");
                            // update the rank for every object in the list rank=TF*IDF
                            rank = tf * IDF;
                            page.append("rank", rank);
                            pagesList.add(page);
                            synchronized (mongo) { // update document with the new list of pages and IDF
                                mongo.updateIDF(IDF, pagesList, WordDocument.get(i).getString("word"));
                            }
                        }
                    }
                }
            }

        }

    }

}
