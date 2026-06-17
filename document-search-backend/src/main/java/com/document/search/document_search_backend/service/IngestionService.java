package com.document.search.document_search_backend.service;

import com.document.search.document_search_backend.model.BookDocument;
import com.document.search.document_search_backend.routing.CustomDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IngestionService {

    @Autowired
    private CustomDispatcher dispatcher;

    @Autowired
    private IngestionWorker ingestionWorker;

    public String coordinateParallelIngestion(List<BookDocument> allBooks) {
        if (allBooks == null || allBooks.isEmpty()) {
            return "Danh sách sách truyền vào trống rỗng!";
        }

        List<String> nodes = dispatcher.getHealthyNodes();
        int nodeCount = nodes.size();

        if (nodeCount == 0) {
            return "Không có Node nào hoạt động để nạp dữ liệu!";
        }

        int totalSize = allBooks.size();
        int chunkSize = (int) Math.ceil((double) totalSize / nodeCount);
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            int fromIndex = i * chunkSize;
            int toIndex = Math.min(fromIndex + chunkSize, totalSize);

            if (fromIndex >= totalSize) break;

            List<BookDocument> subList = new ArrayList<>(allBooks.subList(fromIndex, toIndex));
            String targetNode = nodes.get(i);

            CompletableFuture<String> future = ingestionWorker.workerIngestBatch(subList, targetNode, i + 1);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return "Hệ thống phân tán đã hoàn tất nạp dữ liệu mẫu song song đa luồng! (Tổng số: " + totalSize + " bản ghi dữ liệu thật)";
    }
}