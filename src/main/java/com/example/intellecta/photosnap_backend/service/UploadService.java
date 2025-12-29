package com.example.intellecta.photosnap_backend.service;


import com.example.intellecta.photosnap_backend.model.EvidenceRecord;
import com.example.intellecta.photosnap_backend.model.TrustedDevice;
import com.example.intellecta.photosnap_backend.repository.DeviceRepository;
import com.example.intellecta.photosnap_backend.repository.EvidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class UploadService {

    @Autowired
    private ForensicService forensicService;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private IpfsService ipfsService;

    @Autowired
    private DeviceRepository deviceRepository;

    public ResponseEntity<?> uploadFile(
             MultipartFile image,
             String metadata,
             String signature,
             String publicKey
    ){
        try{

            Optional<TrustedDevice> deviceOpt = deviceRepository.findByPublicKey(publicKey);

            if (deviceOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("UNAUTHORIZED DEVICE: This hardware key is not verified.");
            }

            String deviceId = deviceOpt.get().getDeviceId();

            String imageHash = calculateSha256(image.getBytes());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(image.getBytes());
            outputStream.write(metadata.getBytes(StandardCharsets.UTF_8));
            byte[] dataToVerify = outputStream.toByteArray();

            boolean isAuthentic = forensicService.verifyEvidence(dataToVerify, signature, publicKey);
            if (!isAuthentic) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("TAMPERING DETECTED: Signature does not match the data.");
            }

            String ipfsUrl = ipfsService.uploadToIpfs(image);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(metadata);


            String lat = rootNode.has("lat") ? rootNode.get("lat").asText() : "0.0";
            String lon = rootNode.has("long") ? rootNode.get("long").asText() : "0.0";
            String combinedGps = lat + ", " + lon;

           // String deviceId = rootNode.has("deviceId") ? rootNode.get("deviceId").asText() : "UNKNOWN";
           // String gps = rootNode.has("gps") ? rootNode.get("gps").asText() : "UNKNOWN";

            EvidenceRecord record = new EvidenceRecord();
            record.setContentHash(imageHash);
            record.setDeviceId(deviceId);
            record.setGpsLocation(combinedGps);
            record.setMetaData(metadata);
            record.setPublicKey(publicKey);
            record.setDigitalSignature(signature);
            record.setVerified(true);

            record.setStoragePath(ipfsUrl);

            evidenceRepository.save(record);
            return ResponseEntity.ok("EVIDENCE SECURED: ID " + record.getId());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing evidence: " + e.getMessage());
        }
    }


    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not hash image");
        }
    }
}
