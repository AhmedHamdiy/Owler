package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    List<Node> nodes = new ArrayList<>();

    Node containsNode(String URL) {
        for (Node node : nodes) {
            if (node.URL.equals(URL))
                return node;
        }
        return null;
    }

    static class Node {
        String URL;
        List<String> outgoingLinks;
        List<Node> refs;
        double pageRank;
        int numOutgoingLinks;

        public Node(String URL, double initialPageRank) {
            this.URL = URL;
            this.pageRank = initialPageRank;
            outgoingLinks = new ArrayList<>();
            refs = new ArrayList<>();
        }
    }
    
}
