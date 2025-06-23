package com.example.service1.service;

import com.example.service1.model.*;
import com.example.service1.repository.BatchRequestRepository;
import com.example.service1.repository.RequestItemRepository;
import com.example.service1.repository.QueueItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipException;
import java.util.Arrays;

@Service
public class BatchRequestService {
    private static final Logger logger = LoggerFactory.getLogger(BatchRequestService.class);

    private final BatchRequestRepository batchRequestRepository;
    private final RequestItemRepository requestItemRepository;
    private final QueueItemRepository queueItemRepository;
    private final XmlValidator xmlValidator;
    private final Service2Client service2Client;

    @Autowired
    public BatchRequestService(
            BatchRequestRepository batchRequestRepository,
            RequestItemRepository requestItemRepository,
            QueueItemRepository queueItemRepository,
            XmlValidator xmlValidator,
            Service2Client service2Client) {
        this.batchRequestRepository = batchRequestRepository;
        this.requestItemRepository = requestItemRepository;
        this.queueItemRepository = queueItemRepository;
        this.xmlValidator = xmlValidator;
        this.service2Client = service2Client;
    }

    @Transactional
    public Long processBatch(MultipartFile file) throws IOException {
        logger.info("Starting batch processing for file: {}", file.getOriginalFilename());

        // Проверяем, что файл не пустой
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        // Создаем запись о пакетном запросе
        BatchRequest batchRequest = new BatchRequest();
        batchRequest.setRequestTime(LocalDateTime.now());
        batchRequest.setRequestName(file.getOriginalFilename());
        batchRequest.setStatus(BatchStatus.PENDING);
        batchRequest.setRequestCount(0);
        batchRequest = batchRequestRepository.save(batchRequest);

        List<RequestItem> requestItems = new ArrayList<>();
        int requestCount = 0;

        try {
            // Проверяем, является ли файл ZIP-архивом
            if (file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
                // Проверяем целостность ZIP-архива
                try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(file.getBytes()))) {
                    ZipEntry entry;
                    int totalFiles = 0;
                    int xmlFiles = 0;
                    int processedFiles = 0;
                    
                    while ((entry = zis.getNextEntry()) != null) {
                        totalFiles++;
                        logger.info("Found entry in ZIP: {} (size: {} bytes, isDirectory: {})", 
                                   entry.getName(), entry.getSize(), entry.isDirectory());
                        
                        if (!entry.isDirectory() && entry.getName().endsWith(".xml")) {
                            xmlFiles++;
                            logger.info("Processing XML file from archive: {}", entry.getName());
                            
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            byte[] buffer = new byte[4096]; // Использовать буфер фиксированного размера
                            int bytesRead;
                            while ((bytesRead = zis.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            String xmlContent = outputStream.toString(java.nio.charset.StandardCharsets.UTF_8); // Преобразовать прочитанные байты в строку с UTF-8
                            logger.debug("XML content length: {}", xmlContent.length());
                            logger.debug("XML content: {}", xmlContent);

                            if (xmlContent.isEmpty()) {
                                logger.warn("Skipping empty XML file: {}", entry.getName());
                                continue; // Пропустить пустой XML-файл
                            }

                            // Валидируем XML
                            if (xmlValidator.isValid(xmlContent)) {
                                RequestItem requestItem = new RequestItem();
                                requestItem.setBatchRequest(batchRequest);
                                requestItem.setRequestName(entry.getName());
                                requestItem.setRequestData(xmlContent);
                                requestItem.setStatus(RequestStatus.PENDING);
                                requestItems.add(requestItem);
                                requestCount++;
                                processedFiles++;
                                logger.info("Valid XML file added to processing queue: {}", entry.getName());
                            } else {
                                logger.warn("Invalid XML file skipped: {} (failed validation)", entry.getName());
                            }
                        } else if (entry.isDirectory()) {
                            logger.info("Skipping directory: {}", entry.getName());
                        } else {
                            logger.info("Skipping non-XML file: {}", entry.getName());
                        }
                    }
                    
                    logger.info("ZIP archive summary: total files={}, XML files={}, processed files={}", 
                               totalFiles, xmlFiles, processedFiles);
                }
            } else if (file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
                // Обрабатываем одиночный XML файл
                byte[] fileBytes = file.getBytes();
                logger.info("Read {} bytes from XML file: {}", fileBytes.length, file.getOriginalFilename());
                
                String xmlContent = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
                logger.info("Processing single XML file: {}", file.getOriginalFilename());
                logger.info("XML content length: {}", xmlContent.length());
                logger.info("XML content: {}", xmlContent);

                if (xmlContent.isEmpty()) {
                    logger.warn("Skipping empty XML file: {}", file.getOriginalFilename());
                    throw new IllegalArgumentException("Uploaded XML file is empty");
                }

                if (xmlValidator.isValid(xmlContent)) {
                    logger.info("XML validation successful for file: {}", file.getOriginalFilename());
                    RequestItem requestItem = new RequestItem();
                    requestItem.setBatchRequest(batchRequest);
                    requestItem.setRequestName(file.getOriginalFilename());
                    requestItem.setRequestData(xmlContent);
                    requestItem.setStatus(RequestStatus.PENDING);
                    requestItems.add(requestItem);
                    requestCount++;
                    logger.info("Valid XML file added to processing queue: {}", file.getOriginalFilename());
                } else {
                    logger.warn("Invalid XML file: {}", file.getOriginalFilename());
                    throw new IllegalArgumentException("Invalid XML file format");
                }
            } else {
                throw new IllegalArgumentException("Unsupported file format. Only XML files and ZIP archives are supported.");
            }

            if (requestCount == 0) {
                throw new IllegalArgumentException("No valid XML files found in the package");
            }

            // Сохраняем все запросы
            requestItems = requestItemRepository.saveAll(requestItems);
            logger.info("Saved {} request items to database", requestItems.size());

            // Создаем элементы очереди
            List<QueueItem> queueItems = new ArrayList<>();
            for (RequestItem requestItem : requestItems) {
                QueueItem queueItem = new QueueItem();
                queueItem.setRequestItem(requestItem);
                queueItem.setProcessTime(LocalDateTime.now());
                queueItem.setPriority(1);
                queueItems.add(queueItem);
            }
            queueItemRepository.saveAll(queueItems);
            logger.info("Created {} queue items", queueItems.size());

            // Обновляем количество запросов в пакете
            batchRequest.setRequestCount(requestCount);
            batchRequest.setStatus(BatchStatus.PROCESSING);
            batchRequestRepository.save(batchRequest);
            logger.info("Updated batch request status to PROCESSING");

            return batchRequest.getId();
        } catch (ZipException e) {
            logger.error("Invalid ZIP archive: {}", e.getMessage());
            batchRequest.setStatus(BatchStatus.FAILED);
            batchRequestRepository.save(batchRequest);
            throw new IllegalArgumentException("Invalid ZIP archive: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing batch: {}", e.getMessage(), e);
            batchRequest.setStatus(BatchStatus.FAILED);
            batchRequestRepository.save(batchRequest);
            throw new IOException("Internal server error during batch processing: " + e.getMessage(), e);
        }
    }

    public static class BatchStatusResponse {
        private String status;
        private int progress;
        private int successCount;
        private int errorCount;

        public BatchStatusResponse(String status, int progress, int successCount, int errorCount) {
            this.status = status;
            this.progress = progress;
            this.successCount = successCount;
            this.errorCount = errorCount;
        }

        // Getters and setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(int errorCount) {
            this.errorCount = errorCount;
        }
    }

    public static class BatchNotFoundException extends RuntimeException {
        public BatchNotFoundException(String message) { super(message); }
    }

    @Transactional(readOnly = true)
    public BatchStatusResponse getBatchStatus(Long batchId) {
        List<com.example.service1.dto.BatchStatusAggregationDTO> resultList = requestItemRepository.getBatchStatusAggregationDTO(batchId);
        if (resultList == null || resultList.isEmpty() || resultList.get(0) == null) {
            throw new BatchNotFoundException("Batch not found");
        }
        
        com.example.service1.dto.BatchStatusAggregationDTO result = resultList.get(0);
        int totalRequests = result.getTotalRequests() != null ? result.getTotalRequests().intValue() : 0;
        int successCount = result.getSuccessCount() != null ? result.getSuccessCount().intValue() : 0;
        int errorCount = result.getErrorCount() != null ? result.getErrorCount().intValue() : 0;
        int progress = totalRequests > 0 ? ((successCount + errorCount) * 100) / totalRequests : 0;
        
        return new BatchStatusResponse(result.getStatus(), progress, successCount, errorCount);
    }
} 