package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class TestMongoDB {
    public static void main(String[] args) {
        MongoDB mongoDB = new MongoDB();
        String pageCollection = "Page";
        String wordCollection = "Word";
        String historyCollection = "History";
        String retrievedCollectio = "Retrieved";

        mongoDB.initializeDatabaseConnection();

        // mongoDB.insertOne(new Document("Name", "Mostafa").append("Age",
        // 21).append("Title", "Engineer"),
        // historyCollection);

        // // test for insert many data in one collection ? ////// DONE ✌️////////
        // List<Document> documents = new ArrayList<>();
        // int count = 4;

        // for (int i = 0; i < count; i++) {
        // ObjectId id = new ObjectId();
        // String name = "Name_" + i;
        // int age = 20 + i; // Example age generation
        // String title = "Title_" + i;
        // Document document = new Document("_id", id).append("Name",
        // name).append("age", age).append("tite", title);
        // documents.add(document);
        // }
        // mongoDB.insetMany(documents, wordCollection);

        // ///////////////////////////////////////////////////////////////////////////////

        // ////////// test for the function that search for the word in collection and
        // ////////// returns the pages as an list of objects//////////// ////// DONE✌️

        // /// add some dummy data
        // List<Document> Documentwords = new ArrayList<>();

        // int pages = 4;
        // int words = 4;

        // for (int i = 0; i < words; i++) {

        // List<Document> documentpages = new ArrayList<>();
        // for (int k = 0; k < pages; k++) {
        // ObjectId id = new ObjectId();
        // String content = "sport";
        // String title = "Title_" + k;
        // Document document = new Document("_id", id).append("content",
        // content).append("tite", title);
        // documentpages.add(document);
        // }

        // Document d = new Document("word", "sport" + i).append("pages",
        // documentpages).append("_id", new ObjectId());
        // Documentwords.add(d);

        // }
        // // get the list of pages

        // mongoDB.insetMany(Documentwords, wordCollection);

        List<Document> iterable = mongoDB.FindWordPages("sports");

        // iterate over the list
        for (Document d : iterable) {
        System.err.println();
        System.err.println();
        System.out.println(d.values());
        System.err.println();
        System.err.println();
        }

        // ////////////////////////////////////////////////////////////////////////////////////////

        // double numOfPagePerWord = mongoDB.getNumPagesInWord("sport1");
        // System.err.println();
        // System.err.println();
        // System.err.println("the num is ...");
        // System.out.println(numOfPagePerWord);
        // System.err.println();
        // System.err.println();


        // mongoDB.updateIDF();
        // System.err.println();
        // System.err.println();
        // System.err.println("DONE");
        // System.err.println();
        mongoDB.closeConnection();
    }

}
