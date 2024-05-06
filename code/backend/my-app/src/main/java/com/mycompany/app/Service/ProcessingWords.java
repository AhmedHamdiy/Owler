package com.mycompany.app.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class ProcessingWords {
    public static boolean isStopWord(String word) {
       Path path = Paths.get("code\\backend\\my-app\\src\\stopWords.txt");
        
       try {
            String content = Files.readString(path);
            String[] stopWords = content.split(",");

            for (String stopWord : stopWords)
                if (stopWord.trim().equalsIgnoreCase(word)) 
                    return true;
                
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
