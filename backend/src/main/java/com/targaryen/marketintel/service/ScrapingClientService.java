package com.targaryen.marketintel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;
import java.util.HashMap;

@Service
public class ScrapingClientService {

    @Value("${mockMode:false}")
    private boolean mockMode;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String SCRAPER_URL = "http://localhost:8000/scrape";

    public String scrape(String targetUrl) {
        if (mockMode) {
            try {
                // The Demo Fallback (Crucial)
                ClassPathResource resource = new ClassPathResource("demo-mock.json");
                JsonNode mockData = objectMapper.readTree(resource.getInputStream());
                return mockData.get("content").asText();
            } catch (Exception e) {
                System.err.println("Failed to load demo-mock.json. Using fallback static string.");
                return "Mock content loaded successfully. Enterprise Suite price changed from $349.99 to $299.99.";
            }
        }

        try {
            Map<String, String> request = new HashMap<>();
            request.put("url", targetUrl);
            Map<String, String> response = restTemplate.postForObject(SCRAPER_URL, request, Map.class);
            return response != null ? response.get("content") : null;
        } catch (Exception e) {
            System.err.println("Live Scraping Failed: " + e.getMessage());
            return "Failed to scrape content.";
        }
    }
}
