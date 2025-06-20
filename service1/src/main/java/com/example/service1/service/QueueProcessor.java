package com.example.service1.service;

import com.example.service1.model.QueueItem;
import com.example.service1.model.RequestItem;
import com.example.service1.model.RequestStatus;
import com.example.service1.repository.QueueItemRepository;
import com.example.service1.repository.RequestItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueueProcessor {

    private final QueueItemRepository queueItemRepository;
    private final RequestItemRepository requestItemRepository;
    private final Service2Client service2Client;
    private static final Logger logger = LoggerFactory.getLogger(QueueProcessor.class);

    @Autowired
    public QueueProcessor(
            QueueItemRepository queueItemRepository,
            RequestItemRepository requestItemRepository,
            Service2Client service2Client) {
        this.queueItemRepository = queueItemRepository;
        this.requestItemRepository = requestItemRepository;
        this.service2Client = service2Client;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processQueue() {
        List<QueueItem> readyItems = queueItemRepository.findReadyToProcess(LocalDateTime.now());
        
        for (QueueItem queueItem : readyItems) {
            RequestItem requestItem = queueItem.getRequestItem();
            int updated = requestItemRepository.updateStatusIfPending(requestItem.getId(), RequestStatus.PROCESSING);
            if (updated == 0) {
                continue;
            }
            requestItem.setStatus(RequestStatus.PROCESSING);
            requestItemRepository.save(requestItem);

            try {
                logger.info("Sending request to Service2 for RequestItem ID: {}", requestItem.getId());
                logger.debug("Request XML data: {}", requestItem.getRequestData());
                int result = service2Client.processRequest(requestItem.getRequestData());
                logger.info("Received result from Service2: {}", result);
                
                switch (result) {
                    case 1:
                        requestItem.setStatus(RequestStatus.SUCCESS);
                        logger.info("Request processed successfully for RequestItem ID: {}", requestItem.getId());
                        break;
                    case 2:
                        requestItem.setStatus(RequestStatus.ERROR);
                        requestItem.setErrorCode(String.valueOf(result));
                        requestItem.setErrorMessage("Validation error");
                        logger.warn("Validation error for RequestItem ID: {}", requestItem.getId());
                        break;
                    case 3:
                        requestItem.setStatus(RequestStatus.ERROR);
                        requestItem.setErrorCode(String.valueOf(result));
                        requestItem.setErrorMessage("Processing error");
                        logger.error("Processing error for RequestItem ID: {}", requestItem.getId());
                        break;
                    case 4:
                        requestItem.setStatus(RequestStatus.ERROR);
                        requestItem.setErrorCode(String.valueOf(result));
                        requestItem.setErrorMessage("Service error");
                        logger.error("Service error for RequestItem ID: {}", requestItem.getId());
                        break;
                    default:
                        requestItem.setStatus(RequestStatus.ERROR);
                        requestItem.setErrorCode(String.valueOf(result));
                        requestItem.setErrorMessage("Unknown error");
                        logger.error("Unknown error code {} for RequestItem ID: {}", result, requestItem.getId());
                }
            } catch (Exception e) {
                logger.error("Error processing request for RequestItem ID: {}: {}", requestItem.getId(), e.getMessage(), e);
                requestItem.setStatus(RequestStatus.ERROR);
                requestItem.setErrorCode("4");
                requestItem.setErrorMessage(e.getMessage());
            }

            requestItemRepository.save(requestItem);
            queueItemRepository.delete(queueItem);
        }
    }
} 