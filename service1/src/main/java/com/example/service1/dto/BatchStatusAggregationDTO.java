package com.example.service1.dto;

public class BatchStatusAggregationDTO {
    private Long totalRequests;
    private Long successCount;
    private Long errorCount;
    private String status;

    public BatchStatusAggregationDTO(Long totalRequests, Long successCount, Long errorCount, String status) {
        this.totalRequests = totalRequests;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.status = status;
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 