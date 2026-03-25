package com.targaryen.marketintel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;

@Service
public class ScrapingClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String SCRAPER_URL = "http://localhost:8000/scrape";

    public String scrape(String targetUrl) {
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
