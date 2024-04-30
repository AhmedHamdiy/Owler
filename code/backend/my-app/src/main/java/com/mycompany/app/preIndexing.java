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

package com.mycompany.app;

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

public class preIndexing extends Thread {

    private int num;
    private int interval;
    private List<Document> pagesCollection;
    private MongoDB mongoDB;
    static String suffixPattern = "(ly|ward|wise|ed|en|er|ing|ize|iseable|ible|al|ant|ary|fulious|ous|ive|less|eer|er|ion|ism|ity|ment|ness|or|sion|ship|th|ful)$";

    public preIndexing(int index, int i, List<Document> doc, MongoDB mongo) {
        num = index;
        interval = i;
        pagesCollection = doc;
        mongoDB = mongo;
    }

    static List<Pair<String, Integer>> filterword(Elements elements) {

        List<Pair<String, Integer>> wordPair = new ArrayList<>();

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

                // ==store the result in a new list of pair that store word with the tagname==//
                for (String word : words) {
                    word = word.replaceAll(suffixPattern, "");
                    if (isStopWord(word) || word == "" || word.length() == 1 || word.length() == 2) {
                        continue;
                    }
                    int tagenum = Switchtages(tagname);

                    Pair<String, Integer> pairName = new Pair<>(word, tagenum);
                    wordPair.add(pairName);
                }

            }
        }
        return wordPair;

    }

    private static Integer Switchtages(String tagName) {
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

    public void run() {

        for (int i = num * interval; i < pagesCollection.size() && i < ((num + 1) * interval); i++) {

            String htmlString = (String) pagesCollection.get(i).get("HTML");
            org.jsoup.nodes.Document document = Jsoup.parse(htmlString);

            /// ============= if the page is indexed ignor it =============///

            Object isIndexed = pagesCollection.get(i).get("isIndexed");
            boolean is_indexed = (boolean) isIndexed;

            if (is_indexed)
                continue;

            // =====crate a map to keep track with the word freq and tagname =========///
            Map<String, Pair<Integer, Integer>> wordFreq = new HashMap<>();

            double numTerms = 0;
            // ==========get the document elemenents=============//
            if (document == null)
                continue;

            Elements elements = document.select("*");

            /// ========go and filter this elements===============//
            List<Pair<String, Integer>> pairWords = filterword(elements);

            // =========loop over the of elements=========//
            for (Pair<String, Integer> word : pairWords) {
                numTerms++;

                if (wordFreq.containsKey(word.getKey())) {
                    int frequency = wordFreq.get(word.getKey()).getKey();
                    frequency++;

                    int tagnum = wordFreq.get(word.getKey()).getValue();
                    if (tagnum > word.getValue())
                        tagnum = word.getValue();

                    wordFreq.put(word.getKey(), new Pair<Integer, Integer>(frequency, tagnum));
                } else {
                    wordFreq.put(word.getKey(), new Pair<Integer, Integer>(1, word.getValue()));
                }

            }

            // =========get the page id ======================//
            Object ID = pagesCollection.get(i).get("_id");
            ObjectId id = (ObjectId) ID;

            int freq; // ===>FREQUENCY OF THE WORD
            double TF; // ===>TF OF THE PAGE

            for (Map.Entry<String, Pair<Integer, Integer>> word : wordFreq.entrySet()) {

                freq = (int) word.getValue().getKey();
                TF = freq / numTerms;

                //// ====create a new document of page for word=============//
                Document newPage = new Document("_id", id).append("frequency",
                        freq).append("TF", TF).append("tag", (int) word.getValue().getValue());

                // synchronized over the map of word ============///
                synchronized (Indexer.WordDoecArr) {
                    /// ==========IF THE WORD IS EIXST IN THE MAP SO WILL UPDATE ONLY ==///////
                    if (Indexer.WordDoecArr.containsKey(word.getKey())) {
                        List<Document> pageList = Indexer.WordDoecArr.get(word.getKey());
                        pageList.add(newPage);
                        synchronized (Indexer.WordDoecArr) {
                            Indexer.WordDoecArr.put(word.getKey(), pageList);
                        }
                        // =========IF NOT EXIST WILL INSERT AN NEW ONE===========//
                    } else {
                        List<Document> pageList = new ArrayList<>();
                        pageList.add(newPage);
                        synchronized (Indexer.WordDoecArr) {
                            Indexer.WordDoecArr.put(word.getKey(), pageList);
                        }

                    }
                }
            }
            synchronized (mongoDB) {
                mongoDB.isIndexed(id);
            }
        }
    }
}
