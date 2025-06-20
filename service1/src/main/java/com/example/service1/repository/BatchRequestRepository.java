package com.example.service1.repository;

import com.example.service1.model.BatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRequestRepository extends JpaRepository<BatchRequest, Long> {
} 