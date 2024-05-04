 package com.mycompany.app;

 import opennlp.tools.stemmer.PorterStemmer;

 public class StemmerTest {
 
   public static void main(String[] args) {
     PorterStemmer stemmer = new PorterStemmer();
 
     // Sample words to test
     String[] words = {"rheas"};
 
     for (String word : words) {
       String stemmedWord = stemmer.stem(word);
       System.out.println("Original Word: " + word + ", Stemmed Word: " + stemmedWord);
     }
   }
 }