// AUTHOR : Mariam Amin

/*this calss for filtering page of words
check if the page not indexed befor :

 * 1- handling all numbers
 * 2- handling the unuseful words in some tages
 * 3- handling special characters
 * 4- remove stop words
 * 5- store one word for all its prefix
 * 6- store data in map of Stirng->(word) List->(Documet that have that word)
 * 7- flage the page thet it has been indexed
 *
 */

package com.mycompany.app.Indexer;

import com.mycompany.app.Service.Pair;
import com.mycompany.app.Service.ProcessingWords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mycompany.app.MongoDB;

import opennlp.tools.stemmer.PorterStemmer;

public class PreIndexing extends Thread {

    private int num;
    private int interval;
    private List<Document> pageCollection;
    private MongoDB mongoDB;
    static String suffixPatternRegex = "(ly|ward|wise|ed|en|er|ing|ize|iseable|ible|al|ant|ary|fulious|ous|ive|less|eer|er|ion|ism|ity|ment|ness|or|sion|ship|th|ful)$";

    public PreIndexing(int index, int i, List<Document> docs, MongoDB mongo) {
        num = index;
        interval = i;
        pageCollection = docs;
        mongoDB = mongo;
    }

    static List<Pair<String, Double>> filterWords(Elements elements) {

        // Stores each valid word extracted from the page, with its tag number
        List<Pair<String, Double>> wordPairs = new ArrayList<>();
        PorterStemmer stemmer = new PorterStemmer();

        for (Element element : elements) {
            String tagName = element.tagName();

            // ============ ignore the words in some tags===================//
            if (tagName.equals("a") || tagName.equals("img") 
                    || tagName.equals("br") || tagName.equals("hr")
                    || tagName.equals("input") || tagName.equals("button"))
                continue;

            // =============remove numbers==================//
            String text = element.ownText().trim().replaceAll("\\b\\d+\\b", "");
            
            // ============== remove special characters=========//
            text = text.replaceAll("[^a-zA-Z\\s]", "");

            if (!text.isEmpty()) {
                // ========= split the text to words==========///
                String[] words = text.split("\\s+");

                // ==store the result in a new list of pair that stores word with tagNum==//
                for (String word : words) {
                    word = word.toLowerCase();
                    //String stemmedWord = stemmer.stem(word);
                    //word = word.replaceAll(suffixPatternRegex, "");
                    word = stemmer.stem(word);

                    if (ProcessingWords.isStopWord(word) || word == "" || word.length() == 1 || word.length() == 2) 
                        continue;
                    
                    double tagNum = getTagNum(tagName);
                    
                    
                    Pair<String, Double> newPair = new Pair<>(word, tagNum);
                    wordPairs.add(newPair);
                }
            }
        }
        return wordPairs;
    }

    private static Double getTagNum(String tagName) {
        if (tagName == "h1")
            return 0.15;
        else if (tagName == "h2")
            return 0.13;
        else if (tagName == "h3")
            return 0.12;
        else if (tagName == "h4")
            return 0.11;
        else if (tagName == "h5")
            return 0.1;
        else if (tagName == "h6")
            return 0.09;
        else if (tagName == "p" || tagName == "span")
            return 0.05;
        else
            return 0.01;
    }

    public void run() {

        for (int i = num * interval; i < pageCollection.size() && i < ((num + 1) * interval); i++) {

            String htmlString = (String) pageCollection.get(i).get("HTML");
            org.jsoup.nodes.Document document = Jsoup.parse(htmlString);

            /// ============= if the page is already indexed, ignore it =============///
            if ((boolean) pageCollection.get(i).get("isIndexed"))
                continue;

            // ===== create a map to keep track of the word frequency and tag number =========//
            Map<String, Pair<Integer, Double>> wordFreq = new HashMap<>();

            double numTerms = 0;

            // ========== get  document elements =============//
            if (document == null)
                continue;

            Elements elements = document.select("*");

            /// ======== filter elements ===============//
            List<Pair<String, Double>> wordPairs = filterWords(elements);

            // =========loop over the of elements=========//
            for (Pair<String, Double> wordPair : wordPairs) {
                numTerms++;

                if (wordFreq.containsKey(wordPair.getKey())) {
                    int frequency = wordFreq.get(wordPair.getKey()).getKey();
                    frequency++;

                    Double tagNum = wordFreq.get(wordPair.getKey()).getValue();
                    if (tagNum < wordPair.getValue())
                        tagNum = wordPair.getValue();

                    wordFreq.put(wordPair.getKey(), new Pair<Integer, Double>(frequency, tagNum));
                } else {
                    wordFreq.put(wordPair.getKey(), new Pair<Integer, Double>(1, wordPair.getValue()));
                }
            }

            // =========get page id ======================//
            Object ID = pageCollection.get(i).get("_id");
            ObjectId id = (ObjectId) ID;

            int freq; // ===>FREQUENCY OF THE WORD
            double TF; // ===>TF OF THE PAGE

            for (Map.Entry<String, Pair<Integer, Double>> wordEntry : wordFreq.entrySet()) {

                freq = (int) wordEntry.getValue().getKey();
                TF = freq / numTerms;

                // ========create a new document of page for word=============//
                Document newPage = new Document("_id", id).append("Frequency",
                        freq).append("TF", TF).append("Tag", wordEntry.getValue().getValue());

                //=====  synchronized over the map of words ============///
                synchronized (Indexer.WordDocArr) {
                    /// ==========IF THE WORD EXISTS IN THE MAP SO WILL UPDATE ONLY ==///////
                    if (Indexer.WordDocArr.containsKey(wordEntry.getKey())) {

                        synchronized (Indexer.WordDocArr) {
                            Indexer.WordDocArr.get(wordEntry.getKey()).add(newPage);
                        }
                        // =========IF IT DOES NOT EXIST, WILL INSERT A NEW ONE===========//
                    } else {
                        List<Document> pageList = new ArrayList<>();
                        pageList.add(newPage);
                        synchronized (Indexer.WordDocArr) {
                            Indexer.WordDocArr.put(wordEntry.getKey(), pageList);
                        }
                    }
                }
            }
            synchronized (mongoDB) {
                mongoDB.setIndexedAsTrue(id);
            }
        }
    }
}
