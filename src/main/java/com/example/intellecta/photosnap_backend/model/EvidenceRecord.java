package com.example.intellecta.photosnap_backend.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity(name = "evidence_records")
@Data
public class EvidenceRecord {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contentHash;

    private String deviceId;

    private String gpsLocation;

    @Column(columnDefinition = "TEXT")
    private String metaData;

    @Column(columnDefinition = "TEXT")
    private String publicKey;

    @Column(columnDefinition = "TEXT")
    private String digitalSignature;

    private boolean isVerified;

    private String storagePath;

    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}

