package com.mycompany.app;
import com.mongodb.client.FindIterable;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.bson.Document;

import org.jsoup.select.Elements;
import com.mongodb.internal.connection.Time;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.*;
import com.mongodb.internal.connection.Time;

public class CrawlerOwL implements Runnable {
    private static final int MAX_NUMBER_PAGES = 6000;
    private static int pendingPagesCount;
    public static HashMap<String, Set<String>> blocked = new HashMap<String, Set<String>>();
    public static ArrayList<String> visitedPages = new ArrayList<String>();
    static MongoDB mongoDB = new MongoDB();
    public static Robot myRobot=new Robot(blocked); 
    //We use LinkedBlockingQueue to ensure multithreading safety when we deal with the pages we want to visit.
    public static BlockingQueue<String> pendingPages = new LinkedBlockingQueue<String>();


    public CrawlerOwL(ArrayList<String> visited, int remainingCount) {
        visitedPages = visited;
        pendingPagesCount=remainingCount;
        mongoDB.initializeDatabaseConnection();
    }

    public void insertPendingPage(String url) {
        synchronized (mongoDB) {
            if (!(pendingPages.contains(url) || visitedPages.contains(url) || pendingPages.size()>6000)) {
                try {
                    mongoDB.insertOne(new org.bson.Document("URL",url),"ToVisit");
                    pendingPages.add(url);
                    pendingPagesCount--;
                    mongoDB.notifyAll();//if there was a thread waiting for a page to crawl
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void insertVisitedPage(String url) {
        synchronized (mongoDB) {
            try {
                mongoDB.insertOne(new org.bson.Document("URL",url),"Visited");
                visitedPages.add(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String getNextPendingPage() {
        synchronized (mongoDB) {
            try {
                String nextURL= mongoDB.getFirstToVisit();
                if (nextURL == null) {
                    //No pages to crawl at this time(waits for any other thread to produce urls to crawl)
                    System.out.println("The "+Thread.currentThread().getName() + " will sleep beacuse it has no pages to crawl\n");
                }
                return nextURL;
            } catch (Exception e) {
                System.err.print(e);
                return null; //An error has occured
            }
        }
    }

    public int getVisitedPagesCount() {
        synchronized (mongoDB) {
            return mongoDB.getVisitedCount();
        }
    }

    public int getPendingPagesCount() {
        synchronized (mongoDB) {
            return mongoDB.getVisitedCount();
        }
    }
    public boolean isVisited(String url) {
        synchronized (visitedPages) {
            return visitedPages.contains(url);
        }
    }

    public boolean isPending(String url) {
        synchronized (pendingPages) {
            return pendingPages.contains(url);
        }
    }

    public void run() {
        while (pendingPagesCount>0) {
        System.out.println("we need to crawl "+pendingPagesCount + " pages\n");
            String nextURL = getNextPendingPage();
            try {
                org.jsoup.nodes.Document doc = downloadPage(nextURL);
                if (doc != null) {
                    Elements elements = doc.select("a[href]"); //select all <a> tags that has the href attribute 
                    for (Element tag : elements) {
                        String url = tag.attr("href"); //get the value of href attribute (URL)
                        url = normalizeURL(url, nextURL); 
                        try {
                            if (url != null && !isPending(url) &&  !isVisited(url)&& pendingPages.size()>6000) {
                                insertPendingPage(url);
                            }
                        } catch (Exception e) {
                            System.out.println("Error: can't add this page to pending pages..");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: error in normalizing the link..");

            }
            synchronized(mongoDB){
                pendingPagesCount=MAX_NUMBER_PAGES-getVisitedPagesCount();
            }
        }
        System.out.println("The "+Thread.currentThread().getName() + " has finished crawling\n");

    }

    private org.jsoup.nodes.Document downloadPage(String url) {
        try {
            if(!myRobot.isSafe(url)) 
                return null;
            Connection myConnection = Jsoup.connect(url);
            myConnection.userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            myConnection.referrer("http://www.google.com");
            org.jsoup.nodes.Document doc = myConnection.get();
            if (myConnection.response().statusCode() == 200) {
                
                String HTMLPage = doc.toString();
                String title = doc.title();
                mongoDB.insertOne(new org.bson.Document("Link",url).append("Title", title)
                                .append("HTML", HTMLPage), "Pages");
                    insertVisitedPage(url);
                    return doc;
                }
                return null;
        } catch (IOException e) {
            return null;
        }
    }

    
    private String normalizeURL(String newURL, String source) {
        try {
            URL url = new URL(source);
            if (newURL.startsWith("./")) {
                newURL = newURL.substring(2);
                newURL = url.getProtocol() + "://" + url.getAuthority() + normalizePath(url) + newURL;
            } 
            else if (newURL.startsWith("javascript:")) //Checks for java pages
                newURL = null;
            else if (newURL.indexOf('#') != -1)
                newURL = newURL.substring(0, newURL.indexOf('#'));
            else if (newURL.indexOf('?') != -1) //ignore queries
                newURL = newURL.substring(0, newURL.indexOf('?'));
            return newURL;
        } catch (Exception e) {
            return null;
        }

    }

    private static URL normalizePath(URL url) throws MalformedURLException {
        String path = url.getPath();
        if (path == null || path.length()==0) {
            return new URL(url.getProtocol(), url.getHost(), "/");
        }
        return url;
    }

}