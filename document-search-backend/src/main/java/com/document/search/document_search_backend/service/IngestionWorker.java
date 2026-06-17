package com.document.search.document_search_backend.service;

import com.document.search.document_search_backend.model.BookDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class IngestionWorker {

    @Autowired
    private RestClient restClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async("taskExecutor")
    public CompletableFuture<String> workerIngestBatch(List<BookDocument> batch, String targetNode, int workerId) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Worker " + workerId + " bắt đầu nạp " + batch.size() + " bản ghi vào Node: " + targetNode);

        try {
            StringBuilder bulkRequestBody = new StringBuilder();
            for (BookDocument book : batch) {
                bulkRequestBody.append("{ \"index\" : { \"_index\" : \"tailieu_index\" } }\n");
                String jsonDoc = objectMapper.writeValueAsString(book);
                bulkRequestBody.append(jsonDoc).append("\n");
            }
            bulkRequestBody.append("\n");

            byte[] utf8Bytes = bulkRequestBody.toString().getBytes(StandardCharsets.UTF_8);

            String responseBody = restClient.post()
                    .uri(targetNode + "/_bulk?refresh=true")
                    .header("Content-Type", "application/x-ndjson; charset=utf-8")
                    .body(utf8Bytes)
                    .retrieve()
                    .body(String.class);

            System.out.println("[" + threadName + "] Worker " + workerId + " đã hoàn thành nạp dữ liệu xuống " + targetNode);
            return CompletableFuture.completedFuture("Worker " + workerId + " SUCCESS");

        } catch (Exception e) {
            System.err.println("LỖI KẾT NỐI tại Worker " + workerId + ": " + e.getMessage());
            return CompletableFuture.completedFuture("Worker " + workerId + " FAILED");
        }
    }
}