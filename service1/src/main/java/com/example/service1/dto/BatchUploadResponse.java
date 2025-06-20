package com.example.service1.dto;

public class BatchUploadResponse {
    private Long batchId;
    private String message;

    public BatchUploadResponse(Long batchId, String message) {
        this.batchId = batchId;
        this.message = message;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 