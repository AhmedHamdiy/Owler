package com.mycompany.app;

import org.bson.Document;


public class TestMongoDB {
    public static void main(String[] args) {
        MongoDB mongoDB = new MongoDB();
        String pageCollection = "Page";
        String wordCollection = "Word";
        String historyCollection = "History";
        String retrievedCollection = "Retrieved";

        mongoDB.initializeDatabaseConnection();
        mongoDB.insertOne(new Document("Name", "Mostafa").append("Age", 21).append("Title", "Engineer"), historyCollection);
        mongoDB.closeConnection();
    }
}
