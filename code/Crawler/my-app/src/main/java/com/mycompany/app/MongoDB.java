package com.mycompany.app;




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

   

    public void closeConnection() {
        mongoClient.close();
        System.out.println("The Connection is closed");
    }
}


