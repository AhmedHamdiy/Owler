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
import org.bson.types.ObjectId;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
// import java.util.Arrays;
import java.util.List;
import java.util.Map;

//

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
        toVisitCollection.find().projection(Projections.include("URL")).map(document -> document.getString("URL"))
                .into(visited);
        return visited.isEmpty() ? null : visited;
    }

    public int checkVisitedThreshold() {
        return (int) visitedCollection.countDocuments();
    }

    public int checkTotalThreshold() {
        return (int) toVisitCollection.countDocuments() + (int) visitedCollection.countDocuments();
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

    public void isIndexed(ObjectId id) { // make the page index true

        Bson updates = Updates.combine(Updates.set("isIndexed", true));
        pageCollection.updateOne(eq("_id", id), updates);

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

}