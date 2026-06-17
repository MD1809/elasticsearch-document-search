package com.document.search.document_search_backend.controller;

import com.document.search.document_search_backend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/api/search")
    public ResponseEntity<String> searchBooks(@RequestParam String keyword) {
        try {
            String searchResultJson = searchService.searchFullText(keyword);
            return ResponseEntity.ok(searchResultJson);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}