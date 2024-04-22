package com.mycompany.app;

import java.util.*;

public class Ranker {
    final private MongoDB mongoDB = new MongoDB();
    final private String pageCollection = "Page";
    final private String wordCollection = "Word";
    final private String historyCollection = "History";
    final private String retrievedCollection = "Retrieved";

    public Ranker() {
        mongoDB.initializeDatabaseConnection();
    }

    void pageRanking(String query) {
        HashMap<String, Double> result = mongoDB.getQueryRelevance(query);
        List <String>sortedLinks = sortLinks(result);
        System.out.println(sortedLinks);
    }
    List<String> sortLinks(HashMap<String,Double> result )
    {
        List<Map.Entry<String, Double>> list = new ArrayList<>(result.entrySet());

        list.sort(new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        List<String> sortedLinks = new ArrayList<>();
        for (Map.Entry<String, Double> entry : list)
            sortedLinks.add(entry.getKey());
        return sortedLinks;
    }

}
