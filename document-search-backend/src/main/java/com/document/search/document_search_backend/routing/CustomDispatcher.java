package com.document.search.document_search_backend.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CustomDispatcher {

    @Value("${app.elasticsearch.nodes}")
    private String rawNodes;

    // Sử dụng CopyOnWriteArrayList để đảm bảo an toàn luồng (Thread-safe) khi chạy đa luồng
    private final List<String> healthyNodes = new CopyOnWriteArrayList<>();
    private final List<String> deadNodes = new CopyOnWriteArrayList<>();
    private final AtomicInteger indexCounter = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        if (rawNodes != null && !rawNodes.isEmpty()) {
            String[] nodeArray = rawNodes.split(",");
            healthyNodes.addAll(Arrays.asList(nodeArray));
        }
        System.out.println("[Dispatcher] Đã kích hoạt cụm điều phối với các Node: " + healthyNodes);
    }

    public String getTargetNode() {
        if (healthyNodes.isEmpty()) {
            throw new RuntimeException("Toàn bộ các Node Elasticsearch trong cụm đã bị sập!");
        }
        int index = Math.abs(indexCounter.getAndIncrement() % healthyNodes.size());
        return healthyNodes.get(index);
    }

    public synchronized void reportFailure(String nodeUrl) {
        if (healthyNodes.contains(nodeUrl)) {
            healthyNodes.remove(nodeUrl);
            deadNodes.add(nodeUrl);
            System.err.println("[Failover] Phát hiện Node sập kết nối: " + nodeUrl + ". Đã loại khỏi luồng định tuyến!");
            System.out.println("Các Node còn sống đang hoạt động: " + healthyNodes);
        }
    }

    // Lấy danh sách các node đang hoạt động để nạp dữ liệu song song
    public List<String> getHealthyNodes() {
        return new ArrayList<>(healthyNodes);
    }
}