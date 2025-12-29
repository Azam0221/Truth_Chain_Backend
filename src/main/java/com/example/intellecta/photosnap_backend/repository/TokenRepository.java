package com.example.intellecta.photosnap_backend.repository;


import com.example.intellecta.photosnap_backend.model.ProvisioningToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<ProvisioningToken, Long> {
    Optional<ProvisioningToken> findByToken(String token);
}