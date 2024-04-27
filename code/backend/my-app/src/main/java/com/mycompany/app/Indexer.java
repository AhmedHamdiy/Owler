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

        // =====retrive the pages =====//
        List<Document> PageDocument = mongo.getCrawllerPages();

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
        for (int i = 0; i < PageDocument.size(); i = i + Quantim) {
            Thread t = new preIndexing(numThread, Quantim, PageDocument, mongo);
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
