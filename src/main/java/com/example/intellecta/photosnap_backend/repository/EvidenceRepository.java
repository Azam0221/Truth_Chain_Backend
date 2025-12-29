package com.example.intellecta.photosnap_backend.repository;


import com.example.intellecta.photosnap_backend.model.EvidenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvidenceRepository extends JpaRepository<EvidenceRecord,Long> {

    Optional<EvidenceRecord> findByContentHash(String contentHash);
}
