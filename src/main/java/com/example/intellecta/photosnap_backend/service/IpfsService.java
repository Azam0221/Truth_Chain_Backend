package com.example.intellecta.photosnap_backend.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@Service
public class IpfsService{

    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.key}")
    private String pinataSecretKey;

    private final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    public String uploadToIpfs(MultipartFile file){
        try{
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("pinata_api_key", pinataApiKey);
            headers.set("pinata_secret_api_key", pinataSecretKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()){
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(PINATA_URL, request, Map.class);

            if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null){
                String ipfsHash = (String) response.getBody().get("IpfsHash");
                return "https://gateway.pinata.cloud/ipfs/" + ipfsHash;
            } 
            else{
                throw new RuntimeException("Pinata Upload Failed: " + response.getStatusCode());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("IPFS Error: " + e.getMessage());
        }
    }
}
