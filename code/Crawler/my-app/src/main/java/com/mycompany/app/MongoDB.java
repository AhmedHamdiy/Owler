package com.mycompany.app;

import com.mongodb.client.*;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.BsonValue;
import org.bson.Document;
import com.mongodb.client.result.InsertOneResult;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Projections.*;

import java.util.*;
import java.lang.Object;

import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;

import static com.mongodb.client.model.Updates.set;
import static java.lang.Math.log;
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
        // mongoClient = MongoClients.create("mongodb+srv://user2000:1234@cluster0.ayv9gt9.mongodb.net/");
        mongoClient = MongoClients.create();

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

    void updateIDF() {
        Bson projection = fields(include("Word", "No_pages"), excludeId());
        MongoCursor<Document> cursor = wordCollection.find().projection(projection).iterator();
        int totalPages = 6000;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            int No_pages = doc.getInteger("No_pages");
            double idf = log(6000.0 / No_pages);
            Bson filter = Filters.eq("Word", doc.getString("Word"));
            wordCollection.updateOne(filter, set("IDF", idf));
        }
    }

    void updateTF() {
        Bson projection = fields(include("Word", "Pages"), excludeId());
        MongoCursor<Document> cursor = wordCollection.find().projection(projection).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String word = doc.getString("Word");
            Object obj = doc.get("Pages");

            for (Document d : (List<Document>) obj) {
                int frequency = d.getInteger("Frequency");
                int totalWords = d.getInteger("Total_Words");
                Bson filter = Filters.and(eq("Word", word), eq("Pages.Doc_Id", d.getInteger("Doc_Id")));
                wordCollection.updateOne(filter, set("Pages.$.TF", (double) frequency / totalWords));
            }
        }


    }

    void updateRank() {
        Bson projection = fields(include("Word", "IDF", "Pages.Doc_Id", "Pages.TF", "Pages.Rank"), excludeId());
        MongoCursor<Document> cursor = wordCollection.find().projection(projection).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            System.out.println(doc.toJson());
            String word = doc.getString("Word");
            double IDF = doc.getDouble("IDF");
            for (Document d1 : (List<Document>) doc.get("Pages")) {
                Bson f = Filters.and(eq("Word", word), eq("Pages.Doc_Id", d1.getInteger("Doc_Id")));
                double TF = d1.getDouble("TF");
                double rank = IDF * TF;
                wordCollection.updateOne(f, set("Pages.$.Rank", rank));
            }
        }
    }

    HashMap<String, Double> getQueryRelevance(String query) {
        String[] queryWords = query.split("\\s+");
        HashMap<String, Double> map = new HashMap<String, Double>();
        for (String word : queryWords) {
            Bson filter = Filters.eq("Word", word);
            Bson projection = fields(include("Pages.Rank", "Pages.Doc_Id", "Pages.Link"), excludeId());
            MongoCursor<Document> cursor = wordCollection.find(filter).projection(projection).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object obj = doc.get("Pages");
                for(Document d:(List<Document>)obj)
                {
                    String link = d.getString("Link");
                    Double rank = d.getDouble("Rank");
                    Double prev = map.get(link);
                    if ( prev == null)
                        map.put(link, rank);
                    else
                        map.put(link, rank + prev);
                }
            }
        }
        return map;
    }


    public void closeConnection() {
        mongoClient.close();
        System.out.println("The Connection is closed");
    }
}


