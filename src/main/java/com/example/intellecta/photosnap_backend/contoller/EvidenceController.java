package com.example.intellecta.photosnap_backend.contoller;


import com.example.intellecta.photosnap_backend.model.EvidenceRecord;
import com.example.intellecta.photosnap_backend.repository.EvidenceRepository;
import com.example.intellecta.photosnap_backend.service.ForensicService;
import com.example.intellecta.photosnap_backend.service.UploadService;
import com.example.intellecta.photosnap_backend.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/evidence")
public class EvidenceController {


    @Autowired
    private UploadService uploadService;

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadEvidence(
        @RequestParam("image") MultipartFile image,
        @RequestParam("metadata") String metadata,
        @RequestParam("signature") String signature,
        @RequestParam("publicKey") String publicKey
    ){
       return uploadService.uploadFile(
                image,
                metadata,
                signature,
                publicKey
        );
    }

    @GetMapping("/verify/{hash}")
    public ResponseEntity<?> verifyEvidence(@PathVariable String hash){
        return verificationService.verifyEvidence(hash);
    }

}
