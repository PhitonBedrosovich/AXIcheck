package com.example.service1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class Service2Client {
    private static final Logger logger = LoggerFactory.getLogger(Service2Client.class);

    private final RestTemplate restTemplate;
    private final String service2Url;

    public Service2Client(
            RestTemplate restTemplate,
            @Value("${service2.url}") String service2Url) {
        this.restTemplate = restTemplate;
        this.service2Url = service2Url;
    }

    public int processRequest(String xmlContent) {
        try {
            logger.info("Sending request to Service2 at URL: {}", service2Url + "/api/process");
            logger.debug("Request XML content: {}", xmlContent);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<String> request = new HttpEntity<>(xmlContent, headers);
            ResponseEntity<Integer> response = restTemplate.exchange(
                service2Url + "/api/process",
                HttpMethod.POST,
                request,
                Integer.class
            );

            logger.info("Received response from Service2 with status code: {}", response.getStatusCode());
            logger.debug("Response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            logger.error("Invalid response from Service2: status={}, body={}", response.getStatusCode(), response.getBody());
            return 4; // Ошибка работы сервиса
        } catch (HttpStatusCodeException e) {
            logger.error("HTTP error from Service2: status={}, response={}", e.getStatusCode(), e.getResponseBodyAsString());
            // Обработка HTTP ошибок
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return 2; // Ошибка валидации данных запроса
            }
            return 4; // Ошибка работы сервиса
        } catch (Exception e) {
            logger.error("Error calling Service2: {}", e.getMessage(), e);
            return 4; // Ошибка работы сервиса
        }
    }
} 