package com.targaryen.marketintel.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.targaryen.marketintel.model.Competitor;
import com.targaryen.marketintel.model.Notification;
import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.repository.CompetitorRepository;
import com.targaryen.marketintel.repository.NotificationRepository;
import com.targaryen.marketintel.repository.SnapshotRepository;
import com.targaryen.marketintel.service.GeminiService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalyzeController {

    private final GeminiService geminiService;
    private final SnapshotRepository snapshotRepository;
    private final CompetitorRepository competitorRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyzeController(GeminiService geminiService,
                             SnapshotRepository snapshotRepository,
                             CompetitorRepository competitorRepository,
                             NotificationRepository notificationRepository) {
        this.geminiService = geminiService;
        this.snapshotRepository = snapshotRepository;
        this.competitorRepository = competitorRepository;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/analyze")
    public String triggerAnalysis() {
        StringBuilder contextBuilder = new StringBuilder();
        
        contextBuilder.append("--- COMPETITOR TIME-SERIES DIFFS ---\n");
        List<Competitor> competitors = competitorRepository.findAll();
        for (Competitor c : competitors) {
            List<Snapshot> snaps = snapshotRepository.findAll().stream()
                .filter(s -> s.getCompetitorId().equals(c.getId()))
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(Collectors.toList());

            contextBuilder.append("\nCompetitor: ").append(c.getName()).append("\n");

            if (snaps.size() >= 2) {
                Snapshot oldSnap = snaps.get(snaps.size() - 2);
                Snapshot newSnap = snaps.get(snaps.size() - 1);
                
                contextBuilder.append("[PAST SNAPSHOT]:\n").append(oldSnap.getRawContent()).append("\n\n");
                contextBuilder.append("[CURRENT SNAPSHOT]:\n").append(newSnap.getRawContent()).append("\n\n");
            } else if (snaps.size() == 1) {
                contextBuilder.append("[CURRENT SNAPSHOT]:\n").append(snaps.get(0).getRawContent()).append("\n\n");
            }
        }

        try {
            ClassPathResource resource = new ClassPathResource("reviews.json");
            String reviewsJson = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            contextBuilder.append("\n--- CUSTOMER SENTIMENT & REVIEWS (MOBILE INDUSTRY) ---\n");
            contextBuilder.append("Analyze these reviews to find unmet needs and cross-reference them with the competitor updates:\n");
            contextBuilder.append(reviewsJson).append("\n\n");
        } catch (Exception e) {
            System.err.println("Could not load reviews.json: " + e.getMessage());
        }

        // Send combined data to Gemini
        String jsonResponse = geminiService.analyzeMarketData(contextBuilder.toString());

        // 1. STRIP MARKDOWN
        String cleanJsonResponse = jsonResponse.trim();
        if (cleanJsonResponse.startsWith("```json")) {
            cleanJsonResponse = cleanJsonResponse.substring(7);
        }
        if (cleanJsonResponse.endsWith("```")) {
            cleanJsonResponse = cleanJsonResponse.substring(0, cleanJsonResponse.length() - 3);
        }
        cleanJsonResponse = cleanJsonResponse.trim();

        try {
            JsonNode rootNode = objectMapper.readTree(cleanJsonResponse);
            
            if (rootNode.has("specific_changes") && rootNode.get("specific_changes").isArray()) {
                for (JsonNode change : rootNode.get("specific_changes")) {
                    String category = change.path("category").asText("");
                    String oldState = change.path("old_state").asText("");
                    String newState = change.path("new_state").asText("");
                    int confidence = change.path("confidence_score").asInt(5);

                    if (confidence >= 7 && !category.equals("UI_UPDATE")) {
                        Notification notification = new Notification();
                        notification.setMessage("Market Shift [" + category + "]: Changed from '" + oldState + "' to '" + newState + "'");
                        notification.setIsRead(false);
                        notification.setTimestamp(LocalDateTime.now());
                        
                        if (category.equals("PRICING") || category.equals("PRODUCT_LAUNCH")) {
                            notification.setScore(10);
                            notification.setImpactLevel("CRITICAL");
                        } else {
                            notification.setScore(7);
                            notification.setImpactLevel("HIGH");
                        }
                        
                        notificationRepository.save(notification);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response for Action Engine: " + e.getMessage());
        }

        return cleanJsonResponse;
    }
}