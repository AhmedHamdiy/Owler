// Author :Mariam Amin
/*
 * Indexer calss :
 * 1- retrive all the pages in data base
 * 2- Multithread as the user enter the number of page the thread will index
 * 3- start the threads to filter all pages and store it into map of <String ,Lsit>
 * 4- wait until all thread finish thier job and join at the end
 * 5- multithread to set the data like IDF and rank each thread with  be responsible for 500 word
 * 6- join threads
 * 7- deal with data base and insert all the list by InsertMany
 */

package com.mycompany.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

public class Indexer {
    public static Map<String, List<Document>> WordDoecArr;
    public static List<String> WordList;
    public static List<Document> ReadyWords;
    private static MongoDB mongo;
    private static int Quantim = -1;

    public static void main(String[] args) throws Exception {

        // ================make the monogo connection================//
        mongo = new MongoDB();
        mongo.initializeDatabaseConnection();
        /// ======make the initialization ===//
        WordDoecArr = new HashMap<>();
        ReadyWords = new ArrayList<>();
        WordList = new ArrayList<>();
        // git number of page for thread//
        long startTime = System.currentTimeMillis();

        System.out.println("Enter Number of page per thread : ");

        while (Quantim < 1)
            try {
                Quantim = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
            } catch (Exception e) {
                System.out.println("Please Enter a Number");
                Quantim = 0;
            }

        // =====retrive the pages that has not been indexed =====//
        List<Document> PageDocument = mongo.getnonIndexedPages();
        if (PageDocument.isEmpty()) {
            mongo.closeConnection();
            long finishTime = System
                    .currentTimeMillis();
            System.out.println("Time taken to indexer :" + (finishTime - startTime) + "ms");
            return;
        }

        // for (Document d : PageDocument) {
        // Object ID = d.get("_id");
        // ObjectId id = (ObjectId) ID;
        // mongo.isIndexed(id);
        // }
        List<Thread> arrThread = new ArrayList<>();
        int numThread = 0;
        // Thread t = new index(0, PageDocument.size(), PageDocument);
        // t.start();
        // t.join();
        for (int i = 0; i < PageCollection.size(); i = i + Quantim) {
            Thread t = new preIndexing(numThread, Quantim, PageCollection, mongo);
            numThread++;
            t.setName("preIndexing Spider " + numThread);
            t.start();
            arrThread.add(t);
        }
        // new index(0, URLS.length, URLS).start();

        for (Thread tt : arrThread) {
            tt.join();
            System.out.println("the Thread " + tt.getName() + " is killed");
        }

        // ==== get all the words form data base and drop wordcollection
        List<Document> words = mongo.getWords();
        for (Document d : words) {
            Object word_val = d.get("word");
            String word_value = (String) word_val;
            Object word_P = d.get("pages");
            List<Document> Word_Pages = (List<Document>) word_P;

            if (WordDoecArr.containsKey(word_val)) {
                List<Document> pageList = Indexer.WordDoecArr.get(word_val);
                pageList.addAll(Word_Pages);

                WordDoecArr.put(word_value, pageList);

                // =========if not exists so you do not need update this document===========//
            } else {

                WordDoecArr.put(word_value, (List<Document>) Word_Pages);

            }
        }
        // ====drop word collection======//
        mongo.dropCollection("Word");

        for (Map.Entry<String, List<Document>> entry : WordDoecArr.entrySet()) {
            WordList.add(entry.getKey());
        }
        System.out.println(WordDoecArr.size());
        System.out.println(WordList.size());

        List<Thread> arrThreads = new ArrayList<>();
        numThread = 0;
        // Thread te = new UpdateIDF(0, WordDocument.size(), WordDocument);
        // te.start();
        // te.join();

        for (int i = 0; i < WordList.size(); i = i + 500) {

            Thread t = new setIDF(numThread, 500);
            numThread++;
            t.setName("Indexer Spider IDF " + numThread);
            t.start();
            arrThreads.add(t);

        }

        for (Thread tt : arrThreads) {
            tt.join();
            System.out.println("the Thread IDF " + tt.getName() + " is killed");
        }
        // ====if no word exists close connection======//
        if (ReadyWords.isEmpty()) {
            mongo.closeConnection();
            long finishTime = System
                    .currentTimeMillis();
            System.out.println("Time taken to indexer :" + (finishTime - startTime) + "ms");
            return;
        }

        System.out.println(ReadyWords.size());
        mongo.insetMany(ReadyWords, "Word");

        mongo.closeConnection();
        System.out.println(ReadyWords.size());
        System.out.println(WordDoecArr.size());
        System.out.println(WordList.size());
        long finishTime = System
                .currentTimeMillis();
        System.out.println("Time taken to indexer :" + (finishTime - startTime) + "ms");

    }
}
