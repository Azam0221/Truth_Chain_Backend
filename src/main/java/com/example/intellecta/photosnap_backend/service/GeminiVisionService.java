package com.example.intellecta.photosnap_backend.service;

import com.example.intellecta.photosnap_backend.model.ScreenDetectionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiVisionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String baseUrl;

    @Value("${gemini.model.vision}")
    private String modelName;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public ScreenDetectionResult analyzeImageForScreen(MultipartFile file){
        try {
            String base64Image = compressAndEncodeImage(file);

            String prompt = """
                Analyze this image for signs that it is a photograph of a digital screen (monitor, phone, TV).
                Look for: Moiré patterns, pixel grids, refresh scan lines, or unnatural glare.
                
                Respond ONLY in JSON:
                {
                  "isScreen": boolean,
                  "confidence": double (0.0-1.0),
                  "reasoning": "string",
                  "indicators": ["string", "string"]
                }
                """;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", "image/jpeg",
                                            "data", base64Image
                                    ))
                            ))
                    )
            );

            String respone = restClient.post()
                    .uri(baseUrl + "/" + modelName + ":generateContent?Key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseResponse(respone);
        }
        catch (Exception e){
            e.printStackTrace();
            return new ScreenDetectionResult(false, 0.0, "Vision Analysis Failed: " + e.getMessage(), List.of("Error"));
        }

    }


    private String compressAndEncodeImage(MultipartFile file) throws Exception {
        BufferedImage original = ImageIO.read(file.getInputStream());

        BufferedImage resized = Scalr.resize(original, Scalr.Method.BALANCED, 1024);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resized,"jpg",os);

        return Base64.getEncoder().encodeToString(os.toByteArray());

    }

    private ScreenDetectionResult parseResponse(String rawJson){
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            text = text.replaceAll("```json|```", "").trim();
            return objectMapper.readValue(text, ScreenDetectionResult.class);
        }
        catch (Exception e){
            return new ScreenDetectionResult(false, 0.0, "Parse Error", List.of("Error"));
        }
    }


}
