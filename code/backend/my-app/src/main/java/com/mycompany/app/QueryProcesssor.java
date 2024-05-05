package com.mycompany.app;

import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCursor;
import com.mycompany.app.Ranker.Ranker;
import com.mycompany.app.Service.QPService;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

@RestController
public class QueryProcesssor {
    private MongoDB mongoDB;
    private Ranker ranker;
    private QPService service;

    public QueryProcesssor(MongoDB mongoDB, Ranker ranker, QPService service){
        this.mongoDB = mongoDB;
        this.ranker = ranker;
        this.service = service;
        mongoDB.initializeDatabaseConnection();
    }

    @PostMapping("/")
    public ResponseEntity<List<Document>> postMethodName(@RequestBody String query) {
        ranker = new Ranker();

        String[] queryWords = query.split("\\s+");
        HashMap<ObjectId, Double> map = new HashMap<ObjectId, Double>();

        for (String queryWord : queryWords) {
            queryWord = queryWord.toLowerCase();
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

        ranker.sortPages(res);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
