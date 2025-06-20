package com.example.service1.controller;

import com.example.service1.service.BatchRequestService;
import com.example.service1.dto.BatchUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchRequestController {
    private static final Logger logger = LoggerFactory.getLogger(BatchRequestController.class);

    private final BatchRequestService batchRequestService;

    @Autowired
    public BatchRequestController(BatchRequestService batchRequestService) {
        this.batchRequestService = batchRequestService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBatch(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("Received batch upload request for file: {}", file.getOriginalFilename());
            Long batchId = batchRequestService.processBatch(file);
            BatchUploadResponse response = new BatchUploadResponse(batchId, "Batch request accepted for processing");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/status/{batchId}")
    public ResponseEntity<?> getBatchStatus(@PathVariable Long batchId) {
        try {
            logger.info("Getting status for batch ID: {}", batchId);
            BatchRequestService.BatchStatusResponse status = batchRequestService.getBatchStatus(batchId);
            return ResponseEntity.ok(status);
        } catch (BatchRequestService.BatchNotFoundException e) {
            logger.error("Batch not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Batch not found");
        } catch (RuntimeException e) {
            logger.error("Error getting batch status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 