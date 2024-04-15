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


    public void initializeDatabaseConnection() {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("Crowler");
        pageCollection = database.getCollection("Pages");
        wordCollection = database.getCollection("Words");
        historyCollection = database.getCollection("History");

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
        }
    }

    public void closeConnection() {
        mongoClient.close();
        System.out.println("The Connection is closed");
    }
}


