package com.mycompany.app.Crawler;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mycompany.app.MongoDB;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;

public class CrawlerOwl implements Runnable {
    private static final int MAX_NUMBER_PAGES = 6000;
    static MongoDB mongodb = new MongoDB();
    // We use HashMap to store the blocked URLs for every website by reading
    // robot.txt.
    public static HashMap<String, Set<String>> blocked = new HashMap<String, Set<String>>();
    // We use HashSet to store the visited pages to ensure that we don't visit the
    // same page twice.
    private Set<String> visitedPages = new HashSet<String>();
    // We use LinkedBlockingQueue to ensure multithreading safety when we deal with
    // the pages we want to visit.
    public static BlockingQueue<String> pendingPages = new LinkedBlockingQueue<String>();
    // We use HashSet to store the compact strings of the visited pages to ensure we don't visit the same page twice.
    private Set<String> compactStrings = new HashSet<String>();

    public CrawlerOwl(Set<String> visited, BlockingQueue<String> pending, Set<String> compact) {
        visitedPages = visited;
        pendingPages = pending;
        compactStrings = compact;
        mongodb.initializeDatabaseConnection();
    }

    public byte[] getSHA(String str) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(str.getBytes());
        return messageDigest.digest();
    }

    public String cryptographic(String html) throws NoSuchAlgorithmException, IOException {
        byte[] hash = getSHA(html);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void run() {
        while (continueCrawling()) {
            String nextURL = getNextPage();
            try {
                org.jsoup.nodes.Document doc = visitPage(nextURL);
                if (doc != null) {
                    Elements elements = doc.select("a[href]"); // Select all <a> tags that has the href attribute
                    for (Element tag : elements) {
                        String url = tag.attr("href"); // Get the value of href attribute (URL)
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
                System.err.println("Error: error in normalizing the link ......!!!");
            }
        }
        mongodb.closeConnection();
        System.out.println("The " + Thread.currentThread().getName() + " has finished crawling\n");
    }

    public void insertPending(String link) {
        synchronized (mongodb) {
            if (!(pendingPages.contains(link) || visitedPages.contains(link)) && canInsert()) {
                try {
                    mongodb.insertOne(new org.bson.Document("Link", link), "ToVisit");
                    pendingPages.add(link);
                    mongodb.notifyAll(); // If there was a thread waiting for a page to crawl
                } catch (Exception e) {
                    System.err.println("Error: error in inserting the pending page..");
                }
            }
        }
    }

    public void insertVisited(String link) {
        synchronized (mongodb) {
            try {
                mongodb.insertOne(new org.bson.Document("Link", link), "Visited");
                visitedPages.add(link);
            } catch (Exception e) {
                System.err.println("Error: error in inserting the visited page..");
                e.printStackTrace();
            }
        }
    }

    public String getNextPage() {
        synchronized (mongodb) {
            try {
                String nextURL = mongodb.getFirstToVisit();
                if (pendingPages.contains(nextURL))
                    pendingPages.remove(nextURL);

                if (nextURL == null) {
                    // No pages to crawl at this time(waits for any other thread to produce urls to
                    // crawl)
                    System.out.println("The " + Thread.currentThread().getName()
                            + " will sleep beacuse it has no pages to crawl\n");
                    wait();
                }
                return nextURL;
            } catch (Exception e) {

                System.err.print("Error: error in getting the next page..");
                return null; // An error has occurred
            }
        }
    }

    public boolean continueCrawling() {
        if (pendingPages == null)
            return true;
        else
            synchronized (mongodb) {
                return mongodb.checkVisitedThreshold() <= MAX_NUMBER_PAGES;
            }
    }

    public boolean canInsert() {
        synchronized (mongodb) {
            return mongodb.checkTotalThreshold() <= MAX_NUMBER_PAGES;
        }
    }

    private org.jsoup.nodes.Document visitPage(String url)
            throws IOException, NoSuchAlgorithmException, URISyntaxException {
        try {
            if (!isSafe(url))
                return null;
            Connection myConnection = Jsoup.connect(url);
            myConnection.userAgent(
                    "Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            myConnection.referrer("http://www.google.com");
            org.jsoup.nodes.Document doc = myConnection.get();
            if (myConnection.response().statusCode() == 200) { // Check if the URL is allowed to be crawled
                String HTMLPage = doc.toString();
                String compactString = cryptographic(HTMLPage);
                String title = doc.title();
                String logo = extractLogo(doc); // Extract logo from the document

                // Check if the page has been visited before
                if (visitedPages.contains(url)) {
                    if (compactStrings.contains(compactString)) {
                        return null;
                    } else {
                        org.bson.Document visitedDoc = mongodb.findOne(new org.bson.Document("Link", url), "Page");
                        String storedCompactString = visitedDoc.getString("compactString");
                        compactStrings.remove(storedCompactString);
                        compactStrings.add(compactString);
                        Bson filter = Filters.eq("Link", url);
                        Bson update = Updates.combine(Updates.set("Title", title), Updates.set("HTML", HTMLPage),
                                Updates.set(
                                        "compactString", compactString),
                                Updates.set("isIndexed", false), Updates.set("Logo", logo));
                        mongodb.updateDocument(filter, update, "Page");
                        return doc;
                    }
                } else {
                    // Insert the new version of the page into the database
                    compactStrings.add(compactString);
                    visitedPages.add(url);
                    mongodb.insertOne(new org.bson.Document("Link", url).append("Title", title)
                            .append("HTML", HTMLPage).append("compactString", compactString)
                            .append("isIndexed", false).append("Logo", logo), "Page");

                    return doc;
                }
            } else
                return null;
        } catch (IOException e) {
            System.err.println("Error: error in visiting the page..");
            return null;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractLogo(org.jsoup.nodes.Document doc) {
        Elements logoElements = doc.select("link[rel~=(?i)^(shortcut|icon|apple-touch-icon)]");
        if (!logoElements.isEmpty()) {
            Element logoElement = logoElements.first();
            String logoUrl = logoElement.attr("href");
            logoUrl = normalizeURL(logoUrl, doc.baseUri());
            if (logoUrl.startsWith("/"))
                logoUrl = "/static/media/default-logo.98592a78.svg";
            return logoUrl;
        } else {
            return "/static/media/default-logo.98592a78.svg"; // The Default Icon
        }
    }

    private String normalizeURL(String newURL, String source) {
        try {
            URI url = new URI(source);
            if (newURL.startsWith("./") || newURL.startsWith("/")) {
                newURL = newURL.substring(newURL.indexOf('/') + 1);
                newURL = normalizePath(url) + newURL;
            } else if (newURL.startsWith("javascript:")) // Checks for java pages
                newURL = null;
            else if (newURL.indexOf('?') != -1) // Ignore queries
                newURL = newURL.substring(0, newURL.indexOf('?'));
            else if (newURL.startsWith("#")) // Ignore anchors (fragments)
                newURL = newURL.substring(0, newURL.indexOf('#'));
            return newURL;
        } catch (Exception e) {
            System.err.println("Error: error in normalizing the link: " + newURL + " ..");
            return null;
        }

    }

    private static URI normalizePath(URI link) throws MalformedURLException, URISyntaxException {
        String path = link.getPath();
        if (path == null || path.length() == 0) {
            return new URI(link.toURL().getProtocol(), link.getHost(), "/");
        }
        return link;
    }

    public Boolean isSafe(String url) throws URISyntaxException {
        try {
            URI myURL = new URI(url);
            Set<String> blockedLinks = null;

            if (blocked.containsKey(myURL.toString())) {
                System.out.println(myURL.toString() + " is already in the blocked URLs.");
                blockedLinks = blocked.get(myURL.toString());
            } else {
                Set<String> robotLinks = new HashSet<>();

                try {
                    URI robotsTextFile = new URI(myURL.toURL().getProtocol() + "://" + myURL.getHost() + "/robots.txt");
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(robotsTextFile.toURL().openStream()));

                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        Boolean userAgentStatus = line.startsWith("User-agent:") && line.contains("*");

                        if (userAgentStatus && line.startsWith("Disallow:")) {
                            String blockedPath = line.substring(10).trim();
                            String blockedURL = myURL.toURL().getProtocol() + "://" + myURL.getHost() + blockedPath;

                            robotLinks.add(blockedURL);
                        }
                    }
                    br.close();
                } catch (MalformedURLException e) {
                    System.err.println("Error In URL : can't read robots.txt for URL: " + myURL.toString());
                    return false;
                } catch (IOException e) {
                    System.err.println("Error: can't read URL: " + myURL.toString());
                    return false;
                }
                blocked.put(myURL.toString(), robotLinks);
                blockedLinks = robotLinks;
            }

            if (blockedLinks == null) {
                return false;
            }

            for (String blockedLink : blockedLinks) {
                if (url.toString().startsWith(blockedLink)) {
                    return false;
                }
            }
            return true;
        } catch (URISyntaxException e) {
            System.err.println();
            return false;
        }
    }
}