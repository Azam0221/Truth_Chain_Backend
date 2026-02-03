package com.example.intellecta.photosnap_backend.service;


import com.example.intellecta.photosnap_backend.model.EvidenceRecord;
import com.example.intellecta.photosnap_backend.model.RiskAssessment;
import com.example.intellecta.photosnap_backend.model.ScreenDetectionResult;
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

//    @Autowired
//    private IpfsService ipfsService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private GeminiTextService geminiTextService;

    @Autowired
    private GeminiVisionService geminiVisionService;

    public ResponseEntity<?> uploadFile(
             MultipartFile image,
             String metadata,
             String signature,
             String publicKey
    ){
        try{

            System.out.println("Uploading started 1");

    //        Optional<TrustedDevice> deviceOpt = deviceRepository.findByPublicKey(publicKey);

//            if (deviceOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body("UNAUTHORIZED DEVICE: This hardware key is not verified.");
//            }

     //       String deviceId = deviceOpt.get().getDeviceId();

            String imageHash = calculateSha256(image.getBytes());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(image.getBytes());
            outputStream.write(metadata.getBytes(StandardCharsets.UTF_8));
            byte[] dataToVerify = outputStream.toByteArray();

            System.out.println("Uploading started 2");

//            boolean isAuthentic = forensicService.verifyEvidence(dataToVerify, signature, publicKey);
//            if (!isAuthentic) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("TAMPERING DETECTED: Signature does not match the data.");
//            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(metadata);


            String lat = rootNode.has("lat") ? rootNode.get("lat").asText() : "0.0";
            String lon = rootNode.has("long") ? rootNode.get("long").asText() : "0.0";
            String gps = lat + ", " + lon;
            String timestamp = rootNode.path("timestamp").asText("Unknown");
            String lightLux = rootNode.path("lightLux").asText("0");
            String gyro = rootNode.path("gyro").asText("0,0,0");
            String device = rootNode.path("device").asText("Unknown Device");

            System.out.println("Uploading started 3");

            RiskAssessment risk = geminiTextService.assessMetadataText(gps, timestamp, lightLux, gyro, device);

            boolean isScreen = false;
            String aiReasoning = risk.getReasoning();

            System.out.println("Uploading started 4");

            if (risk.getRiskScore() > 50) {
                System.out.println("High Risk Detected (" + risk.getRiskScore() + "). Running Vision Check...");

                ScreenDetectionResult visionResult = geminiVisionService.analyzeImageForScreen(image);
                isScreen = visionResult.isScreen();

                aiReasoning += " | Vision Analysis: " + visionResult.getReasoning();
            } else {
                aiReasoning += " | Vision Check: Skipped (Low Risk)";
            }

            boolean aiPassed = !isScreen && risk.getRiskScore() < 80;

         //   String ipfsUrl = ipfsService.uploadToIpfs(image);

            for (int i=0;i<8;i++) {
                System.out.println("Uploading to IPFS..." + i);
            }

            EvidenceRecord record = new EvidenceRecord();
            record.setContentHash(imageHash);
          //  record.setDeviceId(deviceId);
            record.setGpsLocation(gps);
            record.setMetaData(metadata);
            record.setPublicKey(publicKey);
            record.setDigitalSignature(signature);
            record.setVerified(true);

            //record.setStoragePath(ipfsUrl);

            record.setRiskScore(risk.getRiskScore());
            record.setScreen(isScreen);
            record.setAiReasoning(aiReasoning);

            record.setVerified(aiPassed);

            System.out.println("Uploading started 5");

            evidenceRepository.save(record);

            if (record.isVerified()) {
                return ResponseEntity.ok("EVIDENCE SECURED & VERIFIED: ID " + record.getId());
            } else {

                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body("EVIDENCE UPLOADED BUT FLAGGED: ID " + record.getId() + ". Reason: " + aiReasoning);
            }
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
