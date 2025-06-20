package com.example.service1.repository;

import com.example.service1.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {
    long countByPackageIdAndStatus(Long packageId, String status);
} 