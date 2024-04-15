package com.mycompany.app;

import org.bson.Document;


public class TestMongoDB {
    public static void main(String[] args) {
        MongoDB mongoDB = new MongoDB();
        String pageCollection = "Pages";
        String wordCollection= "Words";
        String historyCollection = "History";
        mongoDB.initializeDatabaseConnection();
        mongoDB.insertOne(new Document("Name", "Mostafa").append("Age", 21).append("Title", "Engineer"), historyCollection);
        mongoDB.closeConnection();
    }
}
