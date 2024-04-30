package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    List<Node> nodes = new ArrayList<>();

    Node containsNode(String URL) {
        //System.out.println("    Searching in my " + nodes.size() + " nodes");
        for (Node node : nodes) {
            if (node.URL.equals(URL)) {
                System.out.println("    Search: saying yes");
                return node;
            }
        }
        System.out.println("    Search: saying no");
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
