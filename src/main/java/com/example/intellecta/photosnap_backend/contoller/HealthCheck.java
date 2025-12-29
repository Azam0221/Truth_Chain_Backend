package com.example.intellecta.photosnap_backend.contoller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {

    @GetMapping("/test")
    public String test(){
        return "Hello World This is Coder";
    }
}
