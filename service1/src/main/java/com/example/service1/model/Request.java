package com.example.service1.model;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "name")
    private String name;

    @Column(name = "xml_data", columnDefinition = "TEXT")
    private String xmlData;

    @Column(name = "status")
    private String status; // SUCCESS, ERROR, PENDING

    @Column(name = "error_code")
    private String errorCode;
} 