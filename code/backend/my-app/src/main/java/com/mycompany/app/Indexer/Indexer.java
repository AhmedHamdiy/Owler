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

package com.mycompany.app.Indexer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mycompany.app.MongoDB;
import com.mycompany.app.Service.Pair;

public class Indexer {
    public static Map<Pair<String, String>, List<Document>> WordDocArr;
    public static List<Pair<String, String>> WordList;
    public static List<Document> ReadyWords;
    private static MongoDB mongoDB;
    private static int Quantum = -1;

    public static void main(String[] args) throws Exception {

        // ================ establish mongo connection ================//
        mongoDB = new MongoDB();
        mongoDB.initializeDatabaseConnection();
        /// ====== perform initializations ===//
        WordDocArr = new HashMap<>();
        ReadyWords = new ArrayList<>();
        WordList = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // get number of pages for each thread//
        System.out.println("Enter Number of pages per thread: ");

        while (Quantum < 1)
            try {
                Quantum = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
            } catch (Exception e) {
                System.out.println("Please Enter a Number");
                Quantum = 0;
            }

        // =====retrieve the pages that has not been indexed =====//
        List<Document> pageCollection = mongoDB.getnonIndexedPages();

        if (pageCollection.isEmpty()) {
            mongoDB.closeConnection();
            long finishTime = System.currentTimeMillis();
            System.out.println("Time taken by indexer: " + (finishTime - startTime) + " ms");
            return;
        }

        List<Thread> arrThread = new ArrayList<>();
        int numThread = 0;

        for (int i = 0; i < pageCollection.size(); i = i + Quantum) {
            Thread t = new PreIndexing(numThread, Quantum, pageCollection, mongoDB);
            numThread++;
            t.setName("preIndexing Spider " + numThread);
            t.start();
            arrThread.add(t);
        }

        for (Thread t : arrThread) {
            t.join();
            System.out.println("The thread " + t.getName() + " is done");
        }

        // ==== get all the words form database and drop wordCollection
        List<Document> words = mongoDB.getWords();

        for (Document wordDoc : words) {
            String word = wordDoc.getString("Word");
            String stemmedWord = wordDoc.getString("StemmedWord");
            Pair<String, String> wordPair = new Pair<>(word, stemmedWord);

            Object wordPagesArr = wordDoc.get("Pages");
            List<Document> wordPages = (List<Document>) wordPagesArr;

            if (WordDocArr.containsKey(wordPair)) {
                Indexer.WordDocArr.get(wordPair).addAll(wordPages);

                // =========if it does not exist then no need to update document===========//
            } else {
                WordDocArr.put(wordPair, wordPages);
            }
        }
        // ====drop word collection======//
        mongoDB.dropCollection("Word");

        for (Map.Entry<Pair<String, String>, List<Document>> entry : WordDocArr.entrySet()) {
            WordList.add(entry.getKey());
        }

        System.out.println(WordDocArr.size());
        System.out.println(WordList.size());

        List<Thread> arrThreads = new ArrayList<>();
        numThread = 0;

        long numPages = mongoDB.pageCollection.countDocuments();

        for (int i = 0; i < WordList.size(); i = i + 500) {
            Thread t = new SetIDF(numThread, 500, numPages);
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
            mongoDB.closeConnection();
            long finishTime = System.currentTimeMillis();

            System.out.println("Time taken by indexer: " + (finishTime - startTime) + " ms");
            return;
        }

        System.out.println(ReadyWords.size());
        mongoDB.insertMany(ReadyWords, "Word");

        mongoDB.closeConnection();
        System.out.println(ReadyWords.size());
        System.out.println(WordDocArr.size());
        System.out.println(WordList.size());
        long finishTime = System
                .currentTimeMillis();
        System.out.println("Time taken by indexer: " + (finishTime - startTime) + " ms");

    }
}
