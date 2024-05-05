package com.mycompany.app;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

// import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

import org.bson.BsonValue;
import org.bson.Document;

import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;

import java.util.Objects;
import java.util.Set;

// import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
// import org.bson.types.ObjectId;
// import org.springframework.data.mongodb.core.query.Criteria;
// import org.springframework.data.mongodb.core.query.Update;

// import java.io.*;

// import static java.lang.System.*;
// import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
// import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.eq;
// import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
// import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.MongoException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
        database = mongoClient.getDatabase("SearchEngin");
        pageCollection = database.getCollection("Page");
        wordCollection = database.getCollection("Word");
        historyCollection = database.getCollection("History");

        toVisitCollection = database.getCollection("ToVisit");
        visitedCollection = database.getCollection("Visited");

        retrievedCollection = database.getCollection("Retrieved");

        System.out.println("Connected to Database successfully");
    }

    public void PrintCollectionData(String colName) {

        MongoCollection<Document> collections = database.getCollection("colName");
        try (MongoCursor<Document> cursor = collections.find()
                .iterator()) {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        }
    }

    public void insertOne(Document doc, String collectionName) {
        InsertOneResult result;
        switch (collectionName) {
            case "Page":
                result = pageCollection.insertOne(doc);

                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());

                break;
            case "Word":
                System.err.println();
                System.err.println();
                result = wordCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                System.err.println();
                System.err.println();
                break;
            case "History":
                result = historyCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "Retrieved":
                result = retrievedCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;

            case "Visited":
                result = visitedCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "ToVisit":
                result = toVisitCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
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

    public Set<String> getVisitedPages() {
        Set<String> visited = new HashSet<String>();
        toVisitCollection.find().projection(Projections.include("URL")).map(document -> document.getString("URL")).into(visited);
        return visited.isEmpty() ? null : visited;
    }
    
    public int checkVisitedThreshold() {
        return (int)visitedCollection.countDocuments();
    }

    public int checkTotalThreshold() {
        return (int)toVisitCollection.countDocuments()+(int)visitedCollection.countDocuments();
    }
    public void insetMany(List<Document> ls, String collectionName) {

        InsertManyResult resultmany;

        switch (collectionName) {
            case "Page":
                resultmany = pageCollection.insertMany(ls);
                for (Map.Entry<Integer, BsonValue> entry : resultmany.getInsertedIds().entrySet()) {

                    System.out.println(entry.getValue().asObjectId());
                }
                break;
            case "Word":
                resultmany = wordCollection.insertMany(ls);
                for (Map.Entry<Integer, BsonValue> entry : resultmany.getInsertedIds().entrySet()) {
                    System.out.println(entry.getValue().asObjectId());
                }
                break;
            case "History":
                resultmany = historyCollection.insertMany(ls);
                for (Map.Entry<Integer, BsonValue> entry : resultmany.getInsertedIds().entrySet()) {
                    System.out.println(entry.getValue().asObjectId());
                }
                break;
            case "Retrieved":
                resultmany = retrievedCollection.insertMany(ls);
                for (Map.Entry<Integer, BsonValue> entry : resultmany.getInsertedIds().entrySet()) {
                    System.out.println(entry.getValue().asObjectId());
                }
                break;
        }
    }

    public List<Document> FindWordPages(String word) {
        Document projection = new Document("pages", 1).append("_id", 0);

        FindIterable<Document> iterable = wordCollection.find(eq("word", word)).projection(projection);

        List<Document> result = new ArrayList<>();
        iterable.into(result);
        if (!result.isEmpty()) {
            Document firstDocument = result.get(0);
            List<Document> pages = firstDocument.getList("pages", Document.class);
            return pages;
        }
        return null;
    }

    public void closeConnection() {
        mongoClient.close();
        System.out.println("The Connection is closed");
    }

    public boolean isContainWord(String word) {

        Document doc = wordCollection.find(eq("word", word)).first();
        if (doc == null)
            return false;

        return true;

    }

    public long getNumPagesInWord(String word) { /// work correct 👌
        return FindWordPages(word).size();

    }

    public List<Document> getWords() { /// work correct 👌
        // git all the document of word in database
        FindIterable<Document> iterable = wordCollection.find();
        List<Document> result = new ArrayList<>();
        iterable.into(result);

        return result;
        // List<Document> wordarr = new ArrayList<>();
        // if (!result.isEmpty()) {

        // }

        // for (Document d : wordarr) {
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // System.out.println(d);
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // }

        // double IDF;
        // List<Document> pagesList = new ArrayList<>();

        // // loop over the list of word document
        // for (Document word : result) {
        // // git list of pages of the word
        // List<Document> pages = new ArrayList<>();

        // // Check if the "pages" field exists and is of the correct type
        // if (word.containsKey("pages")) {

        // Object pagesObject = word.get("pages");
        // if (pagesObject instanceof List) {
        // List<?> pagesLists = (List<?>) pagesObject;
        // for (Object page : pagesLists) {
        // if (page instanceof Document)
        // pages.add((Document) page);

        // }
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // System.out.println("i in");
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // System.err.println();
        // // num of pages that have that word
        // double numOfPagePerWord = pages.size();

        // // calculate the IDF
        // IDF = Math.log10(6000 / numOfPagePerWord);
        // /// loop over the pages

        // pagesList = new ArrayList<>();
        // for (Document page : pages) {

        // double rank;
        // double tf = page.getDouble("TF");
        // // update the rank for every object in the list rank=TF*IDF
        // rank = tf * IDF;
        // page.append("rank", rank);
        // pagesList.add(page);

        // // update document with the new list of pages and IDF
        // Bson updates = Updates.combine(Updates.set("IDF", IDF), Updates.set("pages",
        // pagesList));
        // wordCollection.updateOne(eq("word", word.getString("word")), updates);
        // }
        // }
        // }

        // }

    }

    public void updateIDF(double IDF, List<Document> pagesList, String word) {

        Bson updates = Updates.combine(Updates.set("IDF", IDF),
                Updates.set("pages", pagesList));
        wordCollection.updateOne(eq("word", word), updates);

    }

    public void updatePagesList(String word, List<Document> list) {
        Bson updates = Updates.combine(Updates.set("pages", list));
        wordCollection.updateOne(eq("word", word), updates);

    }

    public List<Document> getCrawllerPages() {
        FindIterable<Document> iterable = pageCollection.find();
        List<Document> result = new ArrayList<>();
        iterable.into(result);
        return result;

    }

}