package com.example.intellecta.photosnap_backend.service;


import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class ForensicService {

    public boolean verifyEvidence(byte[] originalData, String signatureStr, String publicKeyStr){
        try{

            //Decode the Base64 keys
            byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);


           // Reconstruct the Public Key object
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            //Verify the Signature
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(originalData);

            return sig.verify(signatureBytes);
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
