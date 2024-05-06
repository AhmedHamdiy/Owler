package com.mycompany.app.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mongodb.client.MongoCursor;
import com.mycompany.app.MongoDB;
import com.mycompany.app.Ranker.Ranker;

@Service
public class QPService {
    private MongoDB mongoDB;
    private Ranker ranker;

    public QPService(MongoDB mongoDB, Ranker ranker) {
        this.mongoDB = mongoDB;
        this.ranker = ranker;
        mongoDB.initializeDatabaseConnection();
    }
        
    public List<Document> search(@RequestBody String query) {
        StringBuilder processedQuery = new StringBuilder();

        String[] queryWords = query.split("\\s+");
        HashMap<ObjectId, Double> map = new HashMap<ObjectId, Double>();

        for (String queryWord : queryWords) {
            queryWord = queryWord.toLowerCase();
            processedQuery.append(queryWord).append(" ");

            if(ProcessingWords.isStopWord(queryWord))
                continue;

            MongoCursor<Document> cursor = mongoDB.getWordPagesCursor(queryWord);

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object obj = doc.get("Pages");
            
                for (Document page : (List<Document>) obj) {
                    ObjectId id = page.getObjectId("_id");
                    Double TF_IDF = page.getDouble("TF_IDF");
                    Double tagNum = page.getDouble("Tag");
                    Double relevance = TF_IDF + tagNum;
                    
                    Double prev = map.get(id);
                    if (prev == null)
                        map.put(id, relevance);
                    else
                        map.put(id, relevance + prev);
                }
            }
        }
        
        List<Map.Entry<ObjectId, Double>> list = new ArrayList<>(map.entrySet());
        List<Document> res = new ArrayList<>();

        for (Map.Entry<ObjectId, Double> entry : list) {
            Document fullPageDoc = mongoDB.findPageById(entry.getKey());
            Double finalRank = entry.getValue() + fullPageDoc.getDouble("PageRank");
            Document resDoc = new Document("Rank", finalRank).append("Link", fullPageDoc.getString("Link"))
            .append("Title", fullPageDoc.getString("Title")); 
            
            res.add(resDoc);
        }

        updateQueryHistory(new String(processedQuery).trim());

        ranker.sortPages(res);
        return res;
    }

    void updateQueryHistory(String query) {
        if (mongoDB.updateQueryHistory(query) == 0) {
            Document newQueryHistory = new Document("Query", query).append("Popularity", 1);
            mongoDB.insertOne(newQueryHistory, "QueryHistory");
        }
    }

    public List<String> getSuggestions(String query) {
        MongoCursor<Document> cursor = mongoDB.getQueryHistoryCursor();
        List<Document> suggestionDocs = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        query = query.trim();

        while(cursor.hasNext()) {
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
}
