package com.example.intellecta.photosnap_backend.repository;

import com.example.intellecta.photosnap_backend.model.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DeviceRepository extends JpaRepository<TrustedDevice,Long> {

    Optional<TrustedDevice> findByPublicKey(String publicKey);
    boolean existsByPublicKey(String publicKey);
}
