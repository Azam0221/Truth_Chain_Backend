package com.example.intellecta.photosnap_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    private int riskScore;
    private String reasoning;
    private List<String> flags;

    public boolean requiresVisionAnalysis(int threshold){
        return riskScore > threshold;
    }
}
