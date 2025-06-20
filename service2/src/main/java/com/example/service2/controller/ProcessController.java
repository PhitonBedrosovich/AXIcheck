package com.example.service2.controller;

import com.example.service2.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class ProcessController {
    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);
    
    private final ProcessService processService;
    
    @Autowired
    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }
    
    @PostMapping(value = "/process", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Integer> processRequest(@RequestBody String xmlRequest) {
        logger.info("Received request: {}", xmlRequest);
        int result = processService.processRequest(xmlRequest);
        logger.info("Processing result: {}", result);

        switch (result) {
            case 1:
                return ResponseEntity.ok(result); // Успешная обработка. http код 200
            case 2:
                return ResponseEntity.badRequest().body(result); // Ошибка валидации данных запроса. http код 400
            case 3:
            case 4:
            default:
                return ResponseEntity.status(500).body(result); // Ошибка обработки запроса или ошибка работы сервиса. http код 500
        }
    }
} 