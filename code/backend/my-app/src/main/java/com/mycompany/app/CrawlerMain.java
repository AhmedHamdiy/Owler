package com.mycompany.app;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

public class CrawlerMain {
    static MongoDB mongdb = new MongoDB();
    public  static ArrayList<String> visitedPages;
    public static final int MAX_NUMBER_PAGES=6000;
    public static final int SEED_URLS_COUNT=10;
    public static void main(String[] args) {

        System.out.println("Enter the Number of Threads : ");
        int ThreadNum = 0;

        while (ThreadNum < 1)
            try {
                ThreadNum = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
            } catch (Exception e) {
                System.out.println("Please Enter a Number");
                ThreadNum = 0;
            }

        mongdb.initializeDatabaseConnection();
        //Get the remaining pages count 
        visitedPages=mongdb.getVisitedPages();
        
        int remainingCount=MAX_NUMBER_PAGES;
        if(visitedPages!=null)
            remainingCount-=visitedPages.size();
        
        if(remainingCount==MAX_NUMBER_PAGES)
            visitedPages=fetchSeed();

    
        //Initialize the crawlerSpider8
        Thread[] threads = new Thread[ThreadNum];
        for (int i = 0; i < ThreadNum; i++) {
    
            threads[i] = new Thread(new CrawlerOwL(visitedPages,remainingCount));
            threads[i].setName("CrawlerSpider ("+Integer.toString(i)+")");
        }

        long startTime = System.currentTimeMillis();
        //Start the crawling process
        for (int i = 0; i < ThreadNum; i++)
            threads[i].start();
        for (int i = 0; i < ThreadNum; i++)
            try {
                
                threads[i].join(); //Wait for all crawlspiders to finish their work
                System.out.println("The owler ["+i+"] has joint.\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        long finishTime = System.currentTimeMillis();
        System.out.println("Time taken to crawl "+ remainingCount+" pages :" + (finishTime - startTime) + "ms");
    }
    private static ArrayList<String> fetchSeed(){
        try{
            String seedFile= ("/media/ahmed/Programming/Programming/Projects/web/APT/Crowler/code/backend/my-app/src/main/java/com/mycompany/app/seed.txt");
            
            ArrayList<String> seeds = new ArrayList<String>();
            try (BufferedReader br = new BufferedReader(new FileReader(seedFile))) {
                String URL;
                while ((URL=br.readLine())!=null) {
                    System.out.println("Seed URL: " + URL); //Test
                    seeds.add(URL);
                    mongdb.insertOne(new Document("URL", URL), "ToVisit");
                }
                return seeds;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}