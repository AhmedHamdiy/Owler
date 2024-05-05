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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.mycompany.app.Pair;

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

    static List<Pair<String, Integer>> filterWords(Elements elements) {

        // Stores each valid word extracted from the page, with its tag number
        List<Pair<String, Integer>> wordPairs = new ArrayList<>();

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

                // ==store the result in a new list of pair that store word with the tagname==//
                for (String word : words) {
                    word = word.replaceAll(suffixPatternRegex, "");

                    if (isStopWord(word) || word == "" || word.length() == 1 || word.length() == 2) {
                        continue;
                    }
                    int tagNum = getTagNum(tagName);

                    Pair<String, Integer> newPair = new Pair<>(word, tagNum);
                    wordPairs.add(newPair);
                }
            }
        }
        return wordPairs;
    }

    private static Integer getTagNum(String tagName) {
        if (tagName == "h1")
            return 1;
        else if (tagName == "h2")
            return 2;
        else if (tagName == "h3")
            return 3;
        else if (tagName == "h4")
            return 4;
        else if (tagName == "h5")
            return 5;
        else if (tagName == "h6")
            return 6;
        else if (tagName == "p" || tagName == "span")
            return 7;
        else
            return 8;
    }

    private static boolean isStopWord(String word) {
       // Path path = Paths.get(
       //         "D:\\term2_sec\\APT\\Project\\My-Part\\Crowler\\code\\backend\\my-app\\src\\main\\java\\com\\mycompany\\app\\stopWords.txt");
       Path path = Paths.get(     "code\\backend\\my-app\\src\\stopWords.txt");
        
       try {
            String content = Files.readString(path);
            String[] stopWords = content.split(",");

            for (String stopWord : stopWords)
                if (stopWord.trim().equalsIgnoreCase(word)) 
                    return true;
                
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void run() {

        for (int i = num * interval; i < pageCollection.size() && i < ((num + 1) * interval); i++) {

            String htmlString = (String) pageCollection.get(i).get("HTML");
            org.jsoup.nodes.Document document = Jsoup.parse(htmlString);

            /// ============= if the page is already indexed, ignore it =============///

            if ((boolean) pageCollection.get(i).get("isIndexed"))
                continue;

            // ===== create a map to keep track of the word frequency and tag number =========//
            Map<String, Pair<Integer, Integer>> wordFreq = new HashMap<>();

            double numTerms = 0;

            // ========== get  document elements =============//
            if (document == null)
                continue;

            Elements elements = document.select("*");

            /// ======== filter elements ===============//
            List<Pair<String, Integer>> wordPairs = filterWords(elements);

            // =========loop over the of elements=========//
            for (Pair<String, Integer> wordPair : wordPairs) {
                numTerms++;

                if (wordFreq.containsKey(wordPair.getKey())) {
                    int frequency = wordFreq.get(wordPair.getKey()).getKey();
                    frequency++;

                    int tagNum = wordFreq.get(wordPair.getKey()).getValue();
                    if (tagNum > wordPair.getValue())
                        tagNum = wordPair.getValue();

                    wordFreq.put(wordPair.getKey(), new Pair<Integer, Integer>(frequency, tagNum));
                } else {
                    wordFreq.put(wordPair.getKey(), new Pair<Integer, Integer>(1, wordPair.getValue()));
                }

            }

            // =========get page id ======================//
            Object ID = pageCollection.get(i).get("_id");
            ObjectId id = (ObjectId) ID;

            int freq; // ===>FREQUENCY OF THE WORD
            double TF; // ===>TF OF THE PAGE

            for (Map.Entry<String, Pair<Integer, Integer>> wordEntry : wordFreq.entrySet()) {

                freq = (int) wordEntry.getValue().getKey();
                TF = freq / numTerms;

                //// ====create a new document of page for word=============//
                Document newPage = new Document("_id", id).append("frequency",
                        freq).append("TF", TF).append("tag", (int) wordEntry.getValue().getValue());

                // synchronized over the map of word ============///
                synchronized (Indexer.WordDocArr) {
                    /// ==========IF THE WORD EXISTS IN THE MAP SO WILL UPDATE ONLY ==///////
                    if (Indexer.WordDocArr.containsKey(wordEntry.getKey())) {
                        //List<Document> pageList = Indexer.WordDoecArr.get(wordEntry.getKey());
                        //pageList.add(newPage);

                        synchronized (Indexer.WordDocArr) {
                            //Indexer.WordDoecArr.put(wordEntry.getKey(), pageList);
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
