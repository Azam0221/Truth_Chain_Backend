package com.example.intellecta.photosnap_backend.repository;

import com.example.intellecta.photosnap_backend.model.VerifyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeirfyCodeRepository extends JpaRepository<VerifyCode,Long> {

}
