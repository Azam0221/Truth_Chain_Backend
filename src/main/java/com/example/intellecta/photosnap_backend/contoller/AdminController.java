package com.example.intellecta.photosnap_backend.contoller;

import com.example.intellecta.photosnap_backend.model.ProvisioningToken;
import com.example.intellecta.photosnap_backend.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TokenRepository tokenRepository;


    @PostMapping("/generate-token")
    public ResponseEntity<?> generateToken() {
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        ProvisioningToken token = new ProvisioningToken();
        token.setToken(code);
        token.setUsed(false);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(token);

        return ResponseEntity.ok("NEW OFFICER OTP: " + code);
    }
}