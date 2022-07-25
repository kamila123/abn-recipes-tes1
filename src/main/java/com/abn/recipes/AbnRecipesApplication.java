package com.abn.recipes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class AbnRecipesApplication {
	public static void main(String[] args) {
		SpringApplication.run(AbnRecipesApplication.class, args);
	}
}
