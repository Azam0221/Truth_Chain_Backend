package com.example.intellecta.photosnap_backend.service;

import com.example.intellecta.photosnap_backend.model.EvidenceRecord;
import com.example.intellecta.photosnap_backend.repository.EvidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Optional;



@Service
public class VerificationService {

    @Autowired
    private EvidenceRepository evidenceRepository;

    public ResponseEntity<?> verifyEvidence(String fileHash){

        Optional<EvidenceRecord> record = evidenceRepository.findByContentHash(fileHash);

        if(record.isPresent()){
            return ResponseEntity.ok(record);
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("VERIFICATION FAILED: This image does not exist on the TruthChain. It may be a deepfake or screenshot.");
        }
    }
}
