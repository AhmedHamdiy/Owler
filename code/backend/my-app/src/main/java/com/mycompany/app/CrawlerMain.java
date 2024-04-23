package com.mycompany.app;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bson.Document;

public class CrawlerMain {
    static MongoDB mongdb = new MongoDB();
    public  static BlockingQueue<String> visitedPages;
    public static BlockingQueue<String> CompactStrings = new LinkedBlockingQueue<>(); 
    public static final int MAX_NUMBER_PAGES=6000;
    public static final String SEED_FILE = "code/backend/my-app/src/seed.txt";

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
    
            threads[i] = new Thread(new CrawlerOwL(visitedPages));
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
    private static BlockingQueue<String> fetchSeed(){
        try{
            
            BlockingQueue<String> seeds = new LinkedBlockingQueue<String>();
            try (BufferedReader br = new BufferedReader(new FileReader(SEED_FILE))) {
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