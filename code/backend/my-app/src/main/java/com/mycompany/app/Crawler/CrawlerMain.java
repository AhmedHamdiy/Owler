package com.mycompany.app.Crawler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bson.Document;
import com.mycompany.app.MongoDB;

public class CrawlerMain {
    static MongoDB mongoDB = new MongoDB();
    public static Set<String> visitedPages;
    public static BlockingQueue<String> pendingPages;
    public static final String SEED_FILE = "code\\backend\\my-app\\src\\seed.txt";
    private static Set<String> compactStrings;

    public static void main(String[] args) {

        System.out.print("Enter the Number of your owls: ");
        int threadNum = 0;

        while (threadNum < 1)
            try {
                threadNum = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
            } catch (Exception e) {
                System.out.println("Enter a valid number.");
                threadNum = 0;
            }

        mongoDB.initializeDatabaseConnection();

        // Fetch the visited pages from the database to continue the crawling process
        // (if it was interrupted)
        visitedPages = mongoDB.getVisitedPages();
        compactStrings = mongoDB.getCompactStrings();
        pendingPages = mongoDB.getPendingPages();

        if (visitedPages == null) // The crawling process is starting from scratch
        {
            pendingPages = fetchSeed(); // Add the seeds to pending pages
            visitedPages = new HashSet<>();
        }
        // Feeding our owls to start the crawling process
        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {

            threads[i] = new Thread(new CrawlerOwl(visitedPages, pendingPages, compactStrings));
            threads[i].setName("Owl (" + Integer.toString(i) + ")");
        }

        // Start the crawling process
        for (int i = 0; i < threadNum; i++)
            threads[i].start();

        // Wait for all owls to finish the crawling process
        for (int i = 0; i < threadNum; i++)
            try {
                threads[i].join();
                System.out.println("The owl [" + i + "] has returned home safe.\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        mongoDB.closeConnection();
    }

    private static BlockingQueue<String> fetchSeed() {
        BlockingQueue<String> seeds = new LinkedBlockingQueue<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(SEED_FILE))) {
            String Link;
            while ((Link = br.readLine()) != null) {
                System.out.println("Seed URL: " + Link); // Test
                seeds.add(Link);
                mongoDB.insertOne(new Document("Link", Link), "ToVisit");
            }
            return seeds;
        } catch (IOException e) {
            System.err.println("Error: Error in reading the seed file.");
            return null;
        }
    }
}
