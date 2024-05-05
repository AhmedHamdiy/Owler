package com.mycompany.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrowlerSpringApplication {
	public static MongoDB mongoDB = new MongoDB();

	public static void main(String[] args) {
		SpringApplication.run(CrowlerSpringApplication.class, args);
		mongoDB.initializeDatabaseConnection();
		System.out.println("Let's go?");
	}
}
