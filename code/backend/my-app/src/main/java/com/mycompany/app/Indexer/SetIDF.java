// AUTHOR: Mariam Amin

/*Class resposible for :
 * 1- get the map of string and list loop over it
 * 2- calulate IDF for each word (log(6000/sizefo(list)))
 * 3-loop over the list calculate rank
 * 4- create a new List of Document store word document in it
 * 5- return to main to insert list in database
 */

package com.mycompany.app.Indexer;

import java.util.List;
import java.lang.Math;
import org.bson.Document;

public class SetIDF extends Thread {
    private int num;
    private int interval;
    private long numPages;

    public SetIDF(int n, int q, long numPages) {
        num = n;
        interval = q;
        this.numPages = numPages;
    }

    public void run() {
        double IDF;

        // loop over the list of word document
        for (int i = num * interval; i < Indexer.WordList.size() && i < ((num + 1) * interval); i++) {
            // git list of pages of the word
            List<Document> pages = Indexer.WordDocArr.get(Indexer.WordList.get(i));
            // num of pages that have that word
            double numOfPagesPerWord = pages.size();
            
            // calculate the IDF
            IDF = Math.log10((double) numPages / numOfPagesPerWord);
            
            // loop over pages
            for (Document page : pages) {
                double TF_IDF;
                double TF = page.getDouble("TF");
                // update the rank for every object in the list rank=TF*IDF
                TF_IDF = TF * IDF;
                page.append("TF_IDF", TF_IDF);
            }
            // =====create a new word document and store it in a list of Document ===//
            Document word = new Document("Word", Indexer.WordList.get(i).getKey()).append("StemmedWord", Indexer.WordList.get(i).getValue())
            .append("Pages", pages).append("IDF", IDF);

            synchronized (Indexer.ReadyWords) {
                Indexer.ReadyWords.add(word);
            }

        }

    }
}
