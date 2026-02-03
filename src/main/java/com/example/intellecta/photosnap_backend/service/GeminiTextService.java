package com.example.intellecta.photosnap_backend.service;

import com.example.intellecta.photosnap_backend.model.RiskAssessment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiTextService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String baseUrl;

    @Value("${gemini.model.text}")
    private String modelName;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();


    public RiskAssessment assessMetadataText(String gps, String timestamp, String lightLux, String gyroscope, String deviceModel) {

        String prompt = String.format("""
            Act as a forensic metadata analyst. Verify if the environmental data matches the physical location context.
            
            Metadata:
            - GPS Coordinates: %s
            - Timestamp: %s
            - Ambient Light: %s lux
            - Device Orientation (Gyro): %s
            - Device Model: %s
            
            Tasks:
            1. LOCATION CONTEXT: Based on the GPS, is this likely an OUTDOOR area (park, road) or INDOOR area (building, station)?
            2. PHYSICS CHECK: 
               - If OUTDOOR: Does light match the sun position? (Noon = Bright).
               - If INDOOR: Are artificial light levels plausible? (Noon can be dark/dim).
            3. DEVICE CONSISTENCY:
               - Is the Gyroscope data consistent with taking a photo? (e.g. Extreme angles might indicate tampering).
            4. ANOMALY DETECTION:
               - Flag if GPS is "Open Field" at Noon but light is 0 (Sensor Covered).
            
            Respond ONLY in JSON:
            {
              "riskScore": integer (0-100),
              "locationContext": "OUTDOOR/INDOOR/UNKNOWN",
              "reasoning": "Brief explanation including Gyro/Light analysis.",
              "flags": ["list", "of", "red", "flags"]
            }
            """, gps, timestamp, lightLux, gyroscope, deviceModel);

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String response = restClient.post()
                    .uri(baseUrl + "/" + modelName + ":generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseGeminiResponse(response);

        } catch (Exception e) {
            e.printStackTrace();
            return new RiskAssessment(50, "AI Service Unreachable: " + e.getMessage(), List.of("System Error"));
        }
    }

    private RiskAssessment parseGeminiResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            text = text.replaceAll("```[a-z]*|```", "").trim();

            return objectMapper.readValue(text, RiskAssessment.class);
        } catch (Exception e) {
            return new RiskAssessment(50, "Parse Error", List.of("Invalid JSON from AI"));
        }
    }
}