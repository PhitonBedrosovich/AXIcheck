package com.example.service1.model;

public enum BatchStatus {
    PENDING,    // Ожидает обработки
    PROCESSING, // В процессе обработки
    COMPLETED,  // Обработка завершена
    FAILED      // Ошибка обработки
} 