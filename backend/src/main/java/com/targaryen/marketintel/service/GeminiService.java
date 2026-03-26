package com.targaryen.marketintel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeMarketData(String context) {
        if (apiKey == null || apiKey.equals("ENTER_YOUR_API_KEY_HERE") || apiKey.isEmpty()) {
            return "{\"error\": \"Gemini API Key is missing. Please set GEMINI_API_KEY environment variable or hardcode it in application.properties.\"}";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String prompt = "You are a senior Market Intelligence Analyst. Compare the past and current website snapshots. " +
                "You must return ONLY a strict, valid JSON object. Do NOT wrap it in markdown blockticks (```json). Ensure all strings are properly escaped.\n" +
                "{\n" +
                "  \"specific_changes\": [\n" +
                "    {\n" +
                "      \"category\": \"(MUST be exactly one of: PRICING, PRODUCT_LAUNCH, MESSAGING, UI_UPDATE)\",\n" +
                "      \"old_state\": \"(What it was before, e.g., '$49/mo' or 'None')\",\n" +
                "      \"new_state\": \"(What it is now, e.g., '$59/mo' or 'New AI Feature')\",\n" +
                "      \"confidence_score\": \"(1-10 on how certain you are this is a strategic change)\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"situational_analysis\": \"(Why did they make these changes?)\",\n" +
                "  \"whitespace\": \"(What is the open market gap left by these changes?)\",\n" +
                "  \"strategy\": \"(Provide positioning advice to counter this)\"\n" +
                "}\n\nContext:\n" + context;

        // Build Payload according to Google Gemini REST Specs
        Map<String, Object> requestBody = new HashMap<>();
        
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> partsMap = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> textMap = new HashMap<>();
        textMap.put("text", prompt);
        parts.add(textMap);
        partsMap.put("parts", parts);
        contents.add(partsMap);
        requestBody.put("contents", contents);

        // Crucial: Enforce JSON response type
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("response_mime_type", "application/json");
        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> contentBlock = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> resParts = (List<Map<String, Object>>) contentBlock.get("parts");
            return (String) resParts.get(0).get("text");
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Gemini API Call Failed: " + e.getMessage() + "\"}";
        }
    }
}
