package com.document.search.document_search_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ElasticsearchConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}