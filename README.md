# XÂY DỰNG HỆ THỐNG TRA CỨU TÀI LIỆU DỰA TRÊN ELASTICSEARCH.

## 1. Giới thiệu Đề tài và Công cụ sử dụng
### 1.1. Mục tiêu đề tài
Đề tài tập trung xây dựng một hệ thống tra cứu sách và tài liệu có khả năng tìm kiếm toàn văn (Full-text search) với tốc độ cao. Dự án áp dụng kiến trúc hệ thống phân tán để giải quyết bài toán đảm bảo tính sẵn sàng cao (High Availability) và khả năng chịu lỗi (Fault Tolerance). Hệ thống lưu trữ được thiết kế để kiểm chứng kịch bản thực tế: khi một máy chủ lưu trữ (Node) bất kỳ bị sập, toàn bộ hệ thống vẫn tiếp tục hoạt động bình thường mà không làm mất mát dữ liệu hay gián đoạn trải nghiệm của người dùng.
### 1.2. Các công nghệ và công cụ sử dụng
Hệ thống sử dụng mô hình kiến trúc phân tách với các thành phần chính sau:

* **1.2.1 Tầng Lưu trữ và Tìm kiếm Phân tán:**
  * **Elasticsearch (v7.17.10):** Cơ sở dữ liệu chính và tìm kiếm phân tán, tự động phân mảnh (Sharding) và sao lưu (Replication) dữ liệu.
  * **Kibana (v7.17.10):** Giao diện trực quan để quản trị cài đặt và theo dõi của cụm phân tán.

* **1.2.2 Tầng Nghiệp vụ (Backend):**
  * **Spring Boot (Java):** Xử lý logic nghiệp vụ và giao tiếp với Elasticsearch qua REST API để lưu trữ, tìm kiếm dữ liệu.

* **1.2.3 Tầng Giao diện (Frontend):**
  * **ReactJS:** Cung cấp giao diện tra cứu tài liệu và tương tác với Backend qua Axios.

* **1.2.4 Môi trường Giả lập Hệ thống Phân tán:**
  * **Docker & Docker Compose:** Đóng gói và thiết lập mạng ảo độc lập, giả lập môi trường chạy nhiều máy chủ (Node) trên một máy tính cá nhân.

---

## 2. Hướng dẫn Cài đặt và Thiết lập các Công cụ

Hệ thống vận hành theo mô hình hỗn hợp: Hạ tầng lưu trữ phân tán (Elasticsearch, Kibana) được cô lập bên trong Docker Containers và mã nguồn ứng dụng là sử dụng Spring Boot và ReactJS.

### 2.1. Chuẩn bị môi trường ban đầu
Để chạy được dự án, máy tính cần cài đặt sẵn các công cụ sau:
* Docker Desktop (Kèm theo tính năng WSL 2 đối với hệ điều hành Windows).
* Java Development Kit (JDK) từ phiên bản 17 trở lên.
* Node.js và trình quản lý gói npm hoặc yarn.

### 2.2. Khởi chạy Cụm lưu trữ phân tán (Elasticsearch và Kibana)

1. Tạo một tệp tin văn bản có tên chính xác là `docker-compose.yml` tại thư mục gốc của dự án.
2. Cấu hình file `docker-compose.yml` để khởi tạo một cụm gồm 3 Nodes Elasticsearch và 1 dịch vụ kibana quản trị như sau:
```yaml
version: '3.8'

networks:
  es-distributed-network:
    driver: bridge

services:
  es-node01:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.17.10
    container_name: es_node01_container
    environment:
      - node.name=es-node01
      - cluster.name=es-distributed-cluster
      - discovery.seed_hosts=es-node02,es-node03
      - cluster.initial_master_nodes=es-node01,es-node02,es-node03
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9200:9200"
    networks:
      - es-distributed-network

  es-node02:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.17.10
    container_name: es_node02_container
    environment:
      - node.name=es-node02
      - cluster.name=es-distributed-cluster
      - discovery.seed_hosts=es-node01,es-node03
      - cluster.initial_master_nodes=es-node01,es-node02,es-node03
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9201:9200"
    networks:
      - es-distributed-network

  es-node03:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.17.10
    container_name: es_node03_container
    environment:
      - node.name=es-node03
      - cluster.name=es-distributed-cluster
      - discovery.seed_hosts=es-node01,es-node02
      - cluster.initial_master_nodes=es-node01,es-node02,es-node03
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9202:9200"
    networks:
      - es-distributed-network

  kibana:
    image: docker.elastic.co/kibana/kibana:7.17.10
    container_name: kibana_container
    ports:
      - "5601:5601"
    environment:
      ELASTICSEARCH_HOSTS: '["http://es-node01:9200","http://es-node02:9200","http://es-node03:9200"]'
    depends_on:
      - es-node01
      - es-node02
      - es-node03
    networks:
      - es-distributed-network
```

3. Mở cửa sổ Terminal và chạy lệnh sau để kích hoạt hệ thống:
```yaml
 docker-compose up -d 
 ```

Để kiểm tra hệ thống đang hoạt động:
* Kiểm tra số lượng Node: Truy cập http://localhost:9200/_cluster/health?pretty (Yêu cầu hiển thị "number_of_nodes": 3 và "status": "green").
* Kiểm tra giao diện quản trị: Truy cập http://localhost:5601 để vào Dashboard của Kibana.

### 2.3. Kết nối cổng với tool khác
* Cấu hình Spring Boot Backend (src/main/resources/application.properties):
```yaml
server.port=8080
elasticsearch.host=localhost
elasticsearch.port=9200
elasticsearch.url=http://localhost:9200
```

* Cấu hình ReactJS Frontend (URL gốc cho Axios):
```yaml 
const API_BASE_URL = "http://localhost:8080/api";
```