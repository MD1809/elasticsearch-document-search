package com.document.search.document_search_backend.service;

import com.document.search.document_search_backend.routing.CustomDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Service
public class SearchService {

    @Autowired
    private CustomDispatcher dispatcher;

    @Autowired
    private RestClient restClient;

    /**
     * Thực hiện tìm kiếm toàn văn và tự động Chịu lỗi (Failover) nếu có Node sập
     */
    public String searchFullText(String keyword) {
        int maxRetries = 3;

        for (int i = 0; i < maxRetries; i++) {
            String targetNode = dispatcher.getTargetNode();
            System.out.println("[Routing] Đang định tuyến truy vấn tìm kiếm đến: " + targetNode);

            try {
                String jsonRequestBody = """
                {
                  "query": {
                    "match": {
                      "content": "%s"
                    }
                  }
                }
                """.formatted(keyword);

                String response = restClient.post()
                        .uri(targetNode + "/tailieu_index/_search")
                        .header("Content-Type", "application/json")
                        .body(jsonRequestBody)
                        .retrieve()
                        .body(String.class);

                return response;

            } catch (ResourceAccessException e) {
                System.err.println("[Failover] Không thể kết nối tới Node: " + targetNode + ". Tiến hành kích hoạt cơ chế chịu lỗi...");
                dispatcher.reportFailure(targetNode);
                System.out.println("[Retry] Đang tự động chuyển hướng và gửi lại truy vấn sang Node khác... (Lần thử: " + (i + 1) + ")");
            }
        }
        throw new RuntimeException("Lỗi hệ thống phân tán: Toàn bộ cụm Elasticsearch không phản hồi!");
    }
}