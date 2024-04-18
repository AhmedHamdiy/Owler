package com.mycompany.app;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonValue;
import org.bson.Document;
import com.mongodb.client.result.InsertOneResult;

import java.util.Objects;

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

import java.util.Arrays;


public class MongoDB {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> pageCollection;
    MongoCollection<Document> wordCollection;
    MongoCollection<Document> historyCollection;
    MongoCollection<Document> retrievedCollection;
    MongoCollection<Document> testCollection;


    public void initializeDatabaseConnection() {
        mongoClient = MongoClients.create("mongodb+srv://user2000:1234@cluster0.ayv9gt9.mongodb.net/");
        database = mongoClient.getDatabase("SearchEngin");
        pageCollection = database.getCollection("Page");
        wordCollection = database.getCollection("Word");
        historyCollection = database.getCollection("History");
        retrievedCollection = database.getCollection("Retrieved");
        testCollection = database.getCollection("Test");

        System.out.println("Connected to Database successfully");
    }


    public void insertOne(Document doc, String collectionName) {
        InsertOneResult result;
        switch (collectionName) {
            case "Page":
                result = pageCollection.insertOne(doc);
                break;
            case "Word":
                result = wordCollection.insertOne(doc);
                break;
            case "History":
                result = historyCollection.insertOne(doc);
                break;
            case "Retrieved":
                result = retrievedCollection.insertOne(doc);
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
        }
    }

    public void closeConnection() {
        mongoClient.close();
        System.out.println("The Connection is closed");
    }
}


