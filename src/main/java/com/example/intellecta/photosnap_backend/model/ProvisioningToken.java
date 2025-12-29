package com.example.intellecta.photosnap_backend.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ProvisioningToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;


    private boolean isUsed;

    private LocalDateTime expiryTime;
}