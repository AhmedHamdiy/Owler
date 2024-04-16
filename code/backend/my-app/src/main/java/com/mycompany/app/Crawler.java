package com.mycompany.app;

import org.bson.Document;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.jsoup.Jsoup;
// Remove the conflicting import statement
// import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import java.util.HashMap;

import java.net.MalformedURLException;
import java.net.URL;


/*
 * Check the requirements for the crawler in the comments below.
 * Check the Sncronization in the code. I don't know if it's correct or not.
 */


 /*Crawler notes:
    - The crawler must not visit the same PAGE more than once. Also, normalize URLs and check if they
    are referring to the same page. (You should use the concept of a compact string as well)
    
    - The crawler can only crawl documents of specific types (HTML is sufficient for the project).
    
    - The crawler must maintain its state so that it can, if interrupted, be started again to crawl the documents
    on the list without revisiting documents that have been previously downloaded.
    
    - Some web administrators choose to exclude some pages from the search such as their web pages check for Robot.txt.
    
    - Provide a multithreaded crawler implementation where the user can control the number of threads
    before starting the crawler. Use synchronization appropriately if needed.

    - Take Care of the choice of your seeds.
    
    - Number of Crawled pages is 6000 pages (for the sake of the project). If you collect them during onecrawl or many crawls, both are accepted.
    
    - The crawler should be able to crawl the web pages and download them to the local disk.
    
    - The crawler should be able to extract the text from the downloaded pages.
    
    - The crawler should be able to extract the links from the downloaded pages.
    
    - The crawler should be able to extract the metadata from the downloaded pages.
    
 */
public class Crawler implements Runnable 
{

    MongoDB mongoDB;
    String pageCollection = "Pages";

    private final int MAX_NUMBER_PAGES=6000;
    private int pagesCrawled=0;
    
    //We use a queue to store the pages that we need to crawl in FCFS order.
    private Queue<String> pendingPages = new ConcurrentLinkedQueue<>();
    
    //We use set beacuse it ensures that there won't be duplicate pages.
    private Set<String> visitedPages = new ConcurrentSkipListSet<>();
    
    //TO-DO: We need to make the user determine the number of threads    
    private final int MAX_NUMBER_THREADS=10;
    
    Crawler(){
        mongoDB=new MongoDB();
        mongoDB.initializeDatabaseConnection();
        fetchSeed();
    }

    public void run() {
        Crawler crawler = new Crawler();
        System.out.println("The spider "+ Thread.currentThread().getName() + " has started..");
        while(pagesCrawled < MAX_NUMBER_PAGES){
            String url = pendingPages.poll();
            if(url == null){
                System.out.println("No more pages to crawl..");
                break;
            }
            
            System.out.println("Crawling page: "+url);

            //Download the page content and title
            String pageContent= downloadPage(url);
            //Check if the page is empty
            if(pageContent.equals("")){
                System.out.println("Error while downloading page: "+url);
                continue;
            }
            String pageTitle = Jsoup.parse(pageContent).title();
            System.out.printf("Page Title: "+ pageTitle); //Test

            insertPageIntoDatabase(url, pageContent, pageTitle);
            
            //Extract the links from the page
            try{
                extractLinks(pageContent);
            } catch (IOException e) {
                System.out.println("Error while extracting links: " + e.getMessage());
            }

            synchronized(visitedPages){ 
                visitedPages.add(url);
            }


            /*
            TO-DO: We need to do some checks before crawling so we will need the following locks
            1- pendingPagesLock ==> to check wheather we reached the max_number
            2- visitedPagesLock ==> to ensure only one thread will crawl this page
            3- popularityLock ==> to ensure only one thread can update the popularity queue 
            */
            
            /*Do the crawling part as following
            1. first, check the maxnumber of pages
            2- then lock pendingPagesLock to make sure that only one thread take tis page
            3- validate the url
            4- check if it's HTML link so we need to ignore any other pages like pages with javascript:
            5- normalize the URL 
            6- check if this url has been visited before by another name
            6- Connect to the URL
            7- download the page
            8- add the urls to pending page ordered by FCFS
            */
            
        }
        System.out.println("Crawling process finished..");
        System.out.println("Total number of pages crawled: "+pagesCrawled);
        mongoDB.closeConnection(); 
    }
    public void insertPageIntoDatabase(String url, String content, String title){
        synchronized(mongoDB){
            synchronized(this){
                String normalizedURL=normalizeUrl(url);
                if(normalizedURL==null){
                    System.out.println("Error while normalizing URL: "+url);
                    return;
                }
                mongoDB.insertOne(new Document("doc_id",pagesCrawled).append("url", normalizedURL).append("content", content)
                .append("title", title), pageCollection);
                pagesCrawled++;
            }
        }
    } 
    public static String normalizeUrl(String url) {
        try {
            URI uri = new URI(url);
            String normalizedUrl = uri.normalize().toString();
            return normalizedUrl;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String downloadPage(String url){
        try{
            URL pageURL = new URL(url);
            Scanner pageScanner = new Scanner(pageURL.openStream());
            String pageContent = "";
            while (pageScanner.hasNextLine()) 
                pageContent += pageScanner.nextLine();
            pageScanner.close();
            return pageContent;
        } catch (MalformedURLException e) {
            System.out.println("Error while downloading page: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error while downloading page: " + e.getMessage());
        }
        return "";
    }

    private boolean isHTML(String url){
        return url.endsWith(".html") || url.endsWith(".htm");
    }

    private void extractLinks(String pageContent) throws IOException{
        org.jsoup.nodes.Document doc = Jsoup.parse(pageContent);
        Elements links = doc.select("a");   
        for (Element link : links) {
            String url = link.attr("href");
            System.out.println("Extracted URL: " + url); //Test
            if(isValid(url)){
                synchronized(pendingPages){ //Not sure if we need to synchronize here
                    pendingPages.add(url);
                }
            }
        }
    }
    private void fetchSeed(){
        try{
            File seedFile= new File("seed.txt");
            Scanner seedScanner=new Scanner(seedFile);
                while (seedScanner.hasNextLine()) {
                    String URL = seedScanner.nextLine();
                    if(isValid(URL)){
                        System.out.println("Seed URL: " + URL); //Test
                        synchronized(pendingPages){ //Not sure if we need to synchronize here
                            pendingPages.add(URL);
                        }
                    }
                }
            seedScanner.close();
        } catch (FileNotFoundException e) {
                System.out.println("Error while reading seed file: " + e.getMessage());
            }
    }
    private boolean isValid(String url){
        try {
            new URI(url);
            if(isHTML(url) && !visitedPages.contains(url))
                return true;
            else
                return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
