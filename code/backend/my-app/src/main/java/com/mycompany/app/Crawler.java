package com.mycompany.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.URI;
import java.net.URISyntaxException;

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
 *
 * Hello world!
 *
 */

public class Crawler implements Runnable 
{

    //TO-DO: We need to connect the crawler with mongoDB wla eh?
    
    //Queue or prioirty queue?
    private Queue<String> pendingPages = new ConcurrentLinkedQueue<>();
    
    //We use set beacuse it ensures that there won't be duplicants pages
    private Set<String> visitedPages = new ConcurrentSkipListSet<>();
    
    //TO-DO: We need to make the user determine the number of threads
    
    private final int MAX_NUMBER_PAGES=6000;
    
    public void run() {
        System.out.println("The spider "+ Thread.currentThread().getName() + " has started..");
         
        while(true){
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
         8- add the urls to pending page ordered by popularity??
         */
        
        System.out.println("Crawling process finished..");
    }
    }
    private void fetchSeed(){
        try{
            File seedFile= new File("seed.txt");
            Scanner seedScanner=new Scanner(seedFile);
                    while (seedScanner.hasNextLine()) {
                        String URL = seedScanner.nextLine();
                        // add it to our pagesQueue
                        pendingPages.add(URL);
                    }
                    seedScanner.close();
                } catch (FileNotFoundException e) {
                    System.out.println("Error while reading seed file: " + e.getMessage());
                }
            }
            private boolean isValid(String url) {
                try {
                    new URI(url);
                    return true;
                } catch (URISyntaxException e) {
                    return false;
                }
            }
}
