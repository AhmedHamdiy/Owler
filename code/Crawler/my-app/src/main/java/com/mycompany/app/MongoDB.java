package com.mycompany.app;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.BsonValue;
import org.bson.Document;
import com.mongodb.client.result.InsertOneResult;

import static com.mongodb.client.model.Projections.*;

import java.util.*;
import java.lang.Object;

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

import javax.print.Doc;


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

    Set<String> searchPhrase(String phrase) {
        String[] words = phrase.split("\\s+");
        Set<String> commonLinks = new HashSet<>();
        List<String> returnedLinks = new ArrayList<>();
        for (String st : words) {
            returnedLinks = getPages(st);
            if (commonLinks.isEmpty()) {
                commonLinks.addAll(returnedLinks);
            } else {
                commonLinks.retainAll(new HashSet<>(returnedLinks));
            }
        }
        return commonLinks;
    }

    List<String> getPages(String word) {
        List<String> pages = new ArrayList<>();
        FindIterable<Document> pageDocs;
        Bson filter = Filters.eq("Word", word);
        Bson projection = fields(include("Pages.Link"), excludeId());
        pageDocs = wordCollection.find(filter).projection(projection);

        List<Document> linkDocs = (List<Document>) (pageDocs.first().get("Pages"));
        for (Document doc2 : linkDocs) {
            pages.add(doc2.get("Link", String.class));
        }
        return pages;
    }

    public void closeConnection() {
        mongoClient.close();
        System.out.println("The Connection is closed");
    }
}


