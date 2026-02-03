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
                SYSTEM_ROLE: You are 'TruthChain AI', a forensic metadata analyst. Your job is to calculate the probability of fraud before we spend resources on visual analysis.
                
                            INPUT DATA:
                            - GPS: %s (Check the local time and environment for this coordinate)
                            - UTC Timestamp: %s (Convert to Local Time)
                            - Measured Light: %s Lux (captured via Camera ISO analysis)
                            - Gyroscope: %s (Phone orientation)
                            - Device: %s
                
                            YOUR TASKS (Step-by-Step Reasoning):
                
                            1. CALCULATE SOLAR STATUS:
                               - Based on GPS + Timestamp, is the sun UP or DOWN?
                               - If SUN IS DOWN (Night): Expected natural light is < 50 Lux.
                               - If SUN IS UP (Day): Expected natural light is > 1000 Lux (Outdoors) or > 300 Lux (Indoors).
                
                            2. ANALYZE LIGHT SOURCE:
                               - The user claims this is a real-world photo.
                               - SCENARIO A (Screen Fraud): If it is NIGHT, but Light > 1000 Lux -> Highly suspicious of a screen close-up.
                               - SCENARIO B (Vampire Fraud): If it is NOON (Day), but Light < 50 Lux -> Suspicious of a dark room/basement.
                
                            3. GYRO CHECK (The "Flat" Test):
                               - A Gyro Z-value near 9.8 means the phone is lying flat (like scanning a document).
                               - If GPS implies "Walking" but Gyro is "Perfectly Flat", flag it.
                
                            4. FINAL VERDICT:
                               - Risk Score 0-30: Consistent. (e.g. Day + Bright, Night + Dark).
                               - Risk Score 31-70: Anomalous but possible. (e.g. Night + Bright Streetlamp).
                               - Risk Score 71-100: IMPOSSIBLE Physics. (e.g. Noon + Pitch Black).
                
                            OUTPUT (JSON ONLY):
                            {
                              "riskScore": integer,
                              "locationContext": "e.g., 'New Delhi, Night-time'",
                              "reasoning": "e.g., 'Solar mismatch: It is night in Delhi, but sensor shows daylight levels (2500 Lux). Likely a screen.'",
                              "flags": ["Solar Mismatch", "High Artificial Light", "Gyro Static"]
                            }
            """, gps, timestamp, lightLux, gyroscope, deviceModel);

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json"
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