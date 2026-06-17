package com.document.search.document_search_backend.controller;

import com.document.search.document_search_backend.model.BookDocument;
import com.document.search.document_search_backend.service.IngestionService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class IngestionController {

    @Autowired
    private IngestionService ingestionService;

    @PostMapping("/api/ingest-file")
    public ResponseEntity<String> ingestDataFromFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File tải lên không được để trống!");
        }

        System.out.println("[API] Đang tiếp nhận file dữ liệu thật: " + file.getOriginalFilename());

        try {
            List<BookDocument> realBooks = new ArrayList<>();
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String title = getCellValueAsString(row.getCell(0));
                String author = getCellValueAsString(row.getCell(1));
                String content = getCellValueAsString(row.getCell(2));
                String category = getCellValueAsString(row.getCell(3));

                if (title.isEmpty() || content.isEmpty()) continue;

                String id = UUID.randomUUID().toString();
                realBooks.add(new BookDocument(id, title, author, content, category));
            }
            workbook.close();

            System.out.println("[Parser] Đã đọc thành công " + realBooks.size() + " cuốn sách từ file Excel.");

            String statusMessage = ingestionService.coordinateParallelIngestion(realBooks);

            return ResponseEntity.ok(statusMessage + " (Tổng số: " + realBooks.size() + " bản ghi dữ liệu thật)");

        } catch (Exception e) {
            System.err.println("[Lỗi Hệ Thống] Thất bại khi bóc tách file Excel: " + e.getMessage());
            return ResponseEntity.status(500).body("Có lỗi xảy ra khi xử lý file: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}