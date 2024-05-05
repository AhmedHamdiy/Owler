package com.mycompany.app;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.select.Elements;
import java.util.*;

public class CrawlerOwL implements Runnable {
    private static final int MAX_NUMBER_PAGES = 10000;
    static MongoDB mongodb = new MongoDB();
    //We use HashMap to store the blocked URLs for every website by reading robot.txt.
    public static HashMap<String, Set<String>> blocked = new HashMap<String, Set<String>>();
    //We use HashSet to store the visited pages to ensure that we don't visit the same page twice.
    private Set<String> visitedPages = new HashSet<String>();
    //We use LinkedBlockingQueue to ensure multithreading safety when we deal with the pages we want to visit.
    public static BlockingQueue<String> pendingPages = new LinkedBlockingQueue<String>();


    public CrawlerOwL(Set<String> visited, BlockingQueue<String> pendings) {
        visitedPages = visited;
        pendingPages = pendings;
        mongodb.initializeDatabaseConnection();
    }

    public void run() {
        while (continueCrawling()) {
            String nextURL = getNextPage();
            try {
                org.jsoup.nodes.Document doc = visitPage(nextURL);
                if (doc != null) {
                    Elements elements = doc.select("a[href]"); //Select all <a> tags that has the href attribute 
                    for (Element tag : elements) {
                        String url = tag.attr("href"); //Get the value of href attribute (URL)
                        url = normalizeURL(url, nextURL); 
                        try {
                            if (url != null) {
                                insertPending(url);
                            }
                        } catch (Exception e) {
                            System.err.println("Error: error in adding to pending pages..");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: error in normalizing the link ..");
            }
        }
        mongodb.closeConnection();
        System.out.println("The "+Thread.currentThread().getName() + " has finished crawling\n");
    }

    public void insertPending(String url) {
        synchronized (mongodb) {
            if (!(pendingPages.contains(url) || visitedPages.contains(url)) && canInsert()) {
                try {
                    mongodb.insertOne(new org.bson.Document("URL", url), "ToVisit");
                    pendingPages.add(url);
                    mongodb.notifyAll(); // If there was any thread waiting for a page to crawl
                } catch (Exception e) {
                    System.err.println("Error: error in inserting the pending page..");
                }
            }
        }
    }

    public void insertVisited(String url) {
        synchronized (mongodb) {
            try {
                mongodb.insertOne(new org.bson.Document("URL",url),"Visited");
                visitedPages.add(url);
            } catch (Exception e) {
                System.err.println("Error: error in inserting the visited page..");
                e.printStackTrace();
            }
        }
    }

    public String getNextPage() {
        synchronized (mongodb) {
            try {
                String nextURL= mongodb.getFirstToVisit();
                if (nextURL == null) {
                    //No pages to crawl at this time(waits for any other thread to produce urls to crawl)
                    System.out.println("The "+Thread.currentThread().getName() + " will sleep beacuse it has no pages to crawl\n");
                    wait();
                }
                return nextURL;
            } catch (Exception e) {
                
                System.err.print("Error: error in getting the next page..");
                return null; //An error has occured
            }
        }
    }

    public boolean continueCrawling() {
        if(visitedPages==null)
            return true;
        else
        synchronized (mongodb) {
            return mongodb.checkVisitedThreshold()<= MAX_NUMBER_PAGES;
        }
    }

    public boolean canInsert() {
        synchronized (mongodb) {
            return mongodb.checkTotalThreshold()<= MAX_NUMBER_PAGES;
        }
    }

    private org.jsoup.nodes.Document visitPage(String url) {
        try {
            if(!isSafe(url)) 
                return null;
            Connection myConnection = Jsoup.connect(url);
            myConnection.userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            myConnection.referrer("http://www.google.com");
            org.jsoup.nodes.Document doc = myConnection.get();
            if (myConnection.response().statusCode() == 200) {                    //Check if the URL is allowed to be crawled
                // Extracting the icon URL
                Elements iconElements = doc.select("link[rel~=icon]"); // Selecting link tags with rel attribute containing "icon"
                String iconUrl = null;
                if (!iconElements.isEmpty()) {
                    Element iconElement = iconElements.first();
                    iconUrl = iconElement.attr("href");
                    // Adjust the icon URL if it's relative
                    if (!iconUrl.startsWith("http")) {
                        iconUrl = new URL(new URL(url), iconUrl).toString();
                    }
                }
                String HTMLPage = doc.toString(); //Parsing the HTML page into a string
                String title = doc.title();
                insertVisited(url);
                mongodb.insertOne(new org.bson.Document("Link",url).append("Title", title).append("LogoURL",iconUrl)
                                .append("HTML", HTMLPage).append("isIndexed",false), "Page");
                return doc;
            }
            else
                return null;
        } catch (IOException e) {
            System.err.println("Error: error in visiting the page..");
            return null;
        }
    }
    public static String normalizeURL(String newURL, String source) {
        try {
            URL url = new URL(source);

            if (newURL.startsWith("./")) {
                newURL = newURL.substring(2);
                newURL = url.getProtocol() + "://" + url.getAuthority() + normalizePath(url) + newURL;
            } else if (newURL.startsWith("javascript:")) {
                newURL = null; // Ignore JavaScript links
            } else if (newURL.indexOf('?') != -1) {
                newURL = newURL.substring(0, newURL.indexOf('?')); // Ignore query parameters
            }

            return normalizeURL(newURL); // Normalize the URL
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String normalizeURL(String url) {
        try {
            URL parsedURL = new URL(url.trim());
            //Remove www subdomain if exists (normalize the domain name)
            String domain = parsedURL.getHost().startsWith("www.") ? parsedURL.getHost().substring(4) : parsedURL.getHost();

            //Remove ending slash if exists (normalize the path)
            String path = parsedURL.getPath().endsWith("/") ? parsedURL.getPath().substring(0, parsedURL.getPath().length() - 1) : parsedURL.getPath();

            //Reconstruct the normalized URL
            return parsedURL.getProtocol() + "://" + domain + path;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String normalizePath(URL url) {
        String path = url.getPath();
        if (path.endsWith("/")) {
            return path;
        } else {
            int index = path.lastIndexOf('/');
            if (index != -1) {
                return path.substring(0, index + 1);
            }
            return "/";
        }
    }

    public Boolean isSafe(String url) {
        try {
            URL myURL = new URL(url);
            Set<String> blockedLinks = null;
            //Check if we already have processed this website's robot file before
        if (blocked.containsKey(myURL.toString())) {
            System.out.println(myURL.toString() + " is already in the blocked URLs.");
            blockedLinks= blocked.get(myURL.toString());
        }
        else{
            //This is the first time to go to this website's robot file so let's process it
            Set<String> robotLinks = new HashSet<>();
            try {
                //Get the robots.txt file
                URL robotsTextFile = new URL(myURL.getProtocol() + "://" + myURL.getHost() + "/robots.txt");
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(robotsTextFile.openStream()));
                
                //Read the file and git the blocked urls
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    Boolean userAgentStatus = line.startsWith("User-agent:") && line.contains("*");
                    if (userAgentStatus && line.startsWith("Disallow:")) { //Block this url
                        String blockedPath = line.substring(10).trim();
                        String blockedURL = myURL.getProtocol() + "://" + myURL.getHost() + blockedPath;
                        robotLinks.add(blockedURL);
                    }
                }
                br.close();
            } catch (MalformedURLException e) {
                System.err.println("Error: can't read robots.txt for URL: " + myURL.toString());
                return false;
            } catch (IOException e) {
                System.err.println("Error: can't read URL: " + myURL.toString());
                return false;
            }
            //Add this website blocked links to the blockedLinks hashSet
            blocked.put(myURL.toString(), robotLinks);
            blockedLinks= robotLinks;
        }

        if (blockedLinks == null) {
            return false;
        }

        for (String blockedLink : blockedLinks) {
            if (url.toString().startsWith(blockedLink)) { //If the url is in the blocked URLs block it
                return false;
            }
        }
        return true; //It is safe to crawl
        } catch (MalformedURLException e) {
            System.err.println();
            return false;
        }
    }

}