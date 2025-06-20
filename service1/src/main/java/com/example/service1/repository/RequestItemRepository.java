package com.example.service1.repository;

import com.example.service1.model.BatchRequest;
import com.example.service1.model.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    List<RequestItem> findByBatchRequest(BatchRequest batchRequest);

    @Query("SELECT new com.example.service1.dto.BatchStatusAggregationDTO(" +
           "COUNT(r), " +
           "SUM(CASE WHEN r.status = 'SUCCESS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.status = 'ERROR' THEN 1 ELSE 0 END)) " +
           "FROM RequestItem r WHERE r.batchRequest.id = :batchId")
    List<com.example.service1.dto.BatchStatusAggregationDTO> getBatchStatusAggregationDTO(@Param("batchId") Long batchId);

    @Query("UPDATE RequestItem r SET r.status = :newStatus WHERE r.id = :id AND r.status = 'PENDING'")
    @Modifying
    int updateStatusIfPending(@Param("id") Long id, @Param("newStatus") com.example.service1.model.RequestStatus newStatus);
} 