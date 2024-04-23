package com.mycompany.app;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import org.bson.BsonValue;
import org.bson.Document;
import com.mongodb.client.result.InsertOneResult;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;

import static java.lang.System.*;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.MongoException;

import java.util.ArrayList;
import java.util.Arrays;


public class MongoDB {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> pageCollection;
    MongoCollection<Document> wordCollection;
    MongoCollection<Document> historyCollection;
    MongoCollection<Document> retrievedCollection;
    MongoCollection<Document> visitedCollection;
    MongoCollection<Document> toVisitCollection;


    public void initializeDatabaseConnection() {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("Crowler");
        pageCollection = database.getCollection("Pages");
        wordCollection = database.getCollection("Words");
        historyCollection = database.getCollection("History");
        toVisitCollection = database.getCollection("ToVisit");
        visitedCollection = database.getCollection("Visited");
        retrievedCollection = database.getCollection("Retrieved");

        System.out.println("Connected to Database successfully");
    }


    public void insertOne(Document doc, String collectionName) {
        InsertOneResult result;
        switch (collectionName) {
            case "Pages":
                result = pageCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: " + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "Words":
                result = wordCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: " + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "History":
                result = historyCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: " + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "Retrieved":
                result = retrievedCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: " + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "Visited":
                result = visitedCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: " + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "ToVisit":
                result = toVisitCollection.insertOne(doc);            
                System.out.println("Inserted a document with the following id: " + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
     
        }
    }
    public void dropCollection(String collectionName) {
        switch (collectionName) {
            case "Page":
                pageCollection.drop();
                break;
            case "Word":
                wordCollection.drop();
                break;
            case "History":
                historyCollection.drop();
                break;
            case "Retrieved":
                retrievedCollection.drop();
                break;
            case "Visited":
                visitedCollection.drop();
                break;
            case "ToVisit":
                toVisitCollection.drop();
                break;
        }
    }


    public String getFirstToVisit() throws IOException {
        Document firstToVisit = toVisitCollection.find().limit(1).first();
        if (firstToVisit != null) {
        toVisitCollection.deleteOne(firstToVisit);
        return firstToVisit.getString("URL");
    } else {
        return null;
    }
    }

    public BlockingQueue<String> getVisitedPages() {
        BlockingQueue<String> visited = new LinkedBlockingQueue<String>();
        toVisitCollection.find().projection(Projections.include("URL")).map(document -> document.getString("URL")).into(visited);
        return visited.isEmpty() ? null : visited;
    }

    public int getVisitedCount() {
        return (int)visitedCollection.countDocuments();
    }

    public int getToVisitCount() {
        return (int)toVisitCollection.countDocuments();
    }

    public void closeConnection() {
        mongoClient.close();
        System.out.println("The Connection is closed");
    }
}


