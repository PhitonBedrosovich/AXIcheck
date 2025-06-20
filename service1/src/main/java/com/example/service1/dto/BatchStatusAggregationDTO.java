package com.example.service1.dto;

public class BatchStatusAggregationDTO {
    private Long totalRequests;
    private Long successCount;
    private Long errorCount;

    public BatchStatusAggregationDTO(Long totalRequests, Long successCount, Long errorCount) {
        this.totalRequests = totalRequests;
        this.successCount = successCount;
        this.errorCount = errorCount;
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
} 