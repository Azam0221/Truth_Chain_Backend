package com.example.intellecta.photosnap_backend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenDetectionResult {

    private boolean isScreen;
    private double confidence;
    private String reasoning;
    private List<String> indicators;
}
