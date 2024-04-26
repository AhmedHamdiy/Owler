package com.mycompany.app;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import java.lang.Math;
import org.bson.Document;

import com.mycompany.app.Pair;

public class setIDF extends Thread {
    private int num;
    private int interval;

    public setIDF(int n, int q) {
        num = n;
        interval = q;

    }

    public void run() {
        double IDF;

        // loop over the list of word document
        for (int i = num * interval; i < Indexer.WordList.size() && i < ((num + 1) * interval); i++) {
            // git list of pages of the word
            List<Document> pages = Indexer.WordDoecArr.get(Indexer.WordList.get(i));
            // num of pages that have that word
            double numOfPagePerWord = pages.size();
            // calculate the IDF
            IDF = Math.log10(6000 / numOfPagePerWord);
            /// loop over the pages

            for (Document page : pages) {
                double rank;
                double tf = page.getDouble("TF");
                // update the rank for every object in the list rank=TF*IDF
                rank = tf * IDF;
                page.append("rank", rank);
            }
            // =====create a new word document and store it in a list of Document ===//
            Document word = new Document("word", Indexer.WordList.get(i)).append("pages", pages).append("IDF", IDF);
            
            synchronized (Indexer.ReadyWords) {
                Indexer.ReadyWords.add(word);
            }

        }

    }
}
