package com.document.search.document_search_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DocumentSearchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentSearchBackendApplication.class, args);
	}

}
