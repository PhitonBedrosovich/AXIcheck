package com.example.service1.repository;

import com.example.service1.model.QueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QueueItemRepository extends JpaRepository<QueueItem, Long> {
    @Query("SELECT q FROM QueueItem q WHERE q.processTime <= :currentTime AND q.requestItem.status = 'PENDING' ORDER BY q.priority DESC")
    List<QueueItem> findReadyToProcess(@Param("currentTime") LocalDateTime currentTime);
} 