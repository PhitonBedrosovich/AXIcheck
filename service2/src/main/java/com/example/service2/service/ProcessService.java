package com.example.service2.service;

import org.springframework.stereotype.Service;
import com.example.service2.dto.RequestDTO;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    public int processRequest(String requestBody) {
        logger.info("Received request body: {}", requestBody);
        try {
            logger.info("Creating JAXBContext for RequestDTO");
            JAXBContext jaxbContext = JAXBContext.newInstance(RequestDTO.class);
            logger.info("Creating Unmarshaller");
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            logger.info("Unmarshalling XML to RequestDTO");
            RequestDTO requestDTO = (RequestDTO) jaxbUnmarshaller.unmarshal(new StringReader(requestBody));

            String data = requestDTO.getData();
            String id = requestDTO.getId();

            logger.info("Processing request with id: {} and data: {}", id, data);

            if (data == null || data.trim().isEmpty()) {
                logger.warn("Empty data received for request id: {}", id);
                return 2; // Ошибка валидации данных запроса
            }

            if (data.contains("success")) {
                logger.info("Successfully processed request id: {}", id);
                return 1; // Успешная обработка
            } else if (data.contains("validation_error")) {
                logger.warn("Validation error for request id: {}", id);
                return 2; // Ошибка валидации данных запроса
            } else if (data.contains("processing_error")) {
                logger.error("Processing error for request id: {}", id);
                return 3; // Ошибка обработки запроса
            } else if (data.contains("error")) {
                logger.error("Error in data for request id: {}", id);
                return 3; // Ошибка обработки запроса
            }

            // Если данные не содержат ключевых слов ошибок, считаем обработку успешной
            logger.info("Successfully processed request id: {} with data: {}", id, data);
            return 1;
        } catch (Exception e) {
            logger.error("Service error while processing request. Exception type: {}, Message: {}", 
                e.getClass().getName(), e.getMessage(), e);
            return 4; // Ошибка работы сервиса
        }
    }
} 