package com.mycompany.app.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.Service.QueryProcessor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.bson.Document;

import java.util.*;

@RestController
public class QueryProcesssorController {
    
    private QueryProcessor service;

    public QueryProcesssorController(QueryProcessor service){
        this.service = service;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/search")
    public ResponseEntity<List<Document>> search(@RequestBody String query) {
        return new ResponseEntity<>(service.search(query), HttpStatus.OK); 
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/suggest")
    public ResponseEntity<List<String>> getSuggestions(@RequestBody String query) {
        return new ResponseEntity<>(service.getSuggestions(query), HttpStatus.OK);
    }
}
