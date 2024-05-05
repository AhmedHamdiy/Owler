package com.mycompany.app;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.*;

@RestController
public class QueryProcesssor {
    private static MongoDB mongoDB;
    
    @GetMapping("/hello")
    public String getMethodName() {
        return new String("WELCOME");
    }

    @PostMapping("/")
    public ResponseEntity<List<Document>> postMethodName(@RequestBody String query) {
        mongoDB = CrowlerSpringApplication.mongoDB;
        
        HashMap<ObjectId, Double> result = mongoDB.getQueryRelevance(query);
        
        List<Map.Entry<ObjectId, Double>> list = new ArrayList<>(result.entrySet());

        list.sort(new Comparator<Map.Entry<ObjectId, Double>>() {
            @Override
            public int compare(Map.Entry<ObjectId, Double> o1, Map.Entry<ObjectId, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        List<Document> res = new ArrayList<>();
        for (Map.Entry<ObjectId, Double> entry : list) {
            Document fullPageDoc = mongoDB.findPage(entry.getKey());
            Document resDoc = new Document("_id", entry.getKey()).append("rank", entry.getValue())
            .append("Link", fullPageDoc.getString("Link")).append("Title", fullPageDoc.getString("Title")); 
            
            res.add(resDoc);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    
    
}
