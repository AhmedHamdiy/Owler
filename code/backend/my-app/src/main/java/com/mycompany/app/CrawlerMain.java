package com.mycompany.app;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bson.Document;

public class CrawlerMain {
    static MongoDB mongdb = new MongoDB();
    public  static Set<String> visitedPages;
    public static BlockingQueue<String> pendingPages = new LinkedBlockingQueue<>();
    public static final String SEED_FILE = "code/backend/my-app/src/seed.txt";
    
    public static void main(String[] args) {

        System.out.print("Enter the Number of your owls : ");
        int ThreadNum = 0;

        while (ThreadNum < 1)
            try {
                ThreadNum = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
            } catch (Exception e) {
                System.out.println("Enter a valid number.");
                ThreadNum = 0;
            }

        mongdb.initializeDatabaseConnection();


        //fetch the visited pages from the database to continue the crawling process (if it was interrupted)
        visitedPages=mongdb.getVisitedPages();

        if(visitedPages==null) //The crawling process is starting from scratch
            visitedPages=fetchSeed();//Add the seeds to the pending pages
        
        //Feeding our owls to start the crawling process
        Thread[] threads = new Thread[ThreadNum];
        for (int i = 0; i < ThreadNum; i++) {
    
            threads[i] = new Thread(new CrawlerOwL(visitedPages,pendingPages));
            threads[i].setName("Owl ("+Integer.toString(i)+")");
        }

        //Start the crawling process
        for (int i = 0; i < ThreadNum; i++)
            threads[i].start();
        
        //Wait for all owls to finish the crawling process
        for (int i = 0; i < ThreadNum; i++)
        try {
            threads[i].join(); 
            System.out.println("The owl ["+i+"] has returned home safe.\n");
        } catch (InterruptedException e) {
                e.printStackTrace();
            }

        mongdb.closeConnection();
    }
    private static Set<String> fetchSeed(){
        Set<String> seeds = new HashSet<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(SEED_FILE))) {
            String URL;
            while ((URL=br.readLine())!=null) {
                System.out.println("Seed URL: " + URL); //Test
                seeds.add(URL);
                mongdb.insertOne(new Document("URL", URL), "ToVisit");
            }
            return seeds;
        } catch (IOException e) {
            System.err.println("Error: Error in reading the seed file.");
            return null;
        }
    }
    
    
}