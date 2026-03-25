package com.targaryen.marketintel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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

        // Construct System Prompt Enforcing Strict JSON Output
        String prompt = "You are a senior Market Intelligence Analyst. Analyze the provided context and return ONLY a strict JSON object with this exact schema (no markdown blocks, no extra text):\n" +
                "{\n" +
                "  \"whitespace\": \"(Identify any open market gaps)\",\n" +
                "  \"strategy\": \"(Provide strategic positioning advice)\",\n" +
                "  \"pricing_action\": \"(Feasible counter-offer action)\",\n" +
                "  \"evidence\": \"(Extract the exact source text that triggered this insight)\"\n" +
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
