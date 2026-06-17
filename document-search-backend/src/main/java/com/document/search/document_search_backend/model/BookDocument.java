package com.document.search.document_search_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDocument {
    private String id;
    private String title;
    private String author;
    private String content;
    private String category;
}