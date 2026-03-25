package com.targaryen.marketintel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.targaryen.marketintel.model.Notification;
import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.repository.NotificationRepository;
import com.targaryen.marketintel.repository.ProductOfferRepository;
import com.targaryen.marketintel.repository.ReviewRepository;
import com.targaryen.marketintel.repository.SnapshotRepository;
import com.targaryen.marketintel.service.GeminiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalyzeController {

    private final GeminiService geminiService;
    private final SnapshotRepository snapshotRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyzeController(GeminiService geminiService,
                             SnapshotRepository snapshotRepository,
                             ProductOfferRepository productOfferRepository,
                             ReviewRepository reviewRepository,
                             NotificationRepository notificationRepository) {
        this.geminiService = geminiService;
        this.snapshotRepository = snapshotRepository;
        this.productOfferRepository = productOfferRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/analyze")
    public String triggerAnalysis() {
        StringBuilder contextBuilder = new StringBuilder();
        
        contextBuilder.append("--- RECENT COMPETITOR CHANGES ---\n");
        List<Snapshot> snapshots = snapshotRepository.findAll();
        if (!snapshots.isEmpty()) {
            contextBuilder.append("Latest Scraped Data: \n").append(snapshots.get(snapshots.size() - 1).getRawContent()).append("\n");
        }
        
        contextBuilder.append("\n--- INTERNAL PRODUCT OFFERS ---\n");
        productOfferRepository.findAll().forEach(p -> 
            contextBuilder.append("Offer: ").append(p.getName())
                          .append(", Our Price: ").append(p.getCurrentPrice())
                          .append(", Competitor Price: ").append(p.getCompetitorPrice()).append("\n")
        );

        contextBuilder.append("\n--- USER REVIEWS / SENTIMENT ---\n");
        reviewRepository.findAll().forEach(r -> 
            contextBuilder.append("Rating: ").append(r.getRating()).append("/5, Feedback: ").append(r.getContent()).append("\n")
        );

        String jsonResponse = geminiService.analyzeMarketData(contextBuilder.toString());

        // Phase 6: Action & Scoring Engine
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("pricing_action") && rootNode.get("pricing_action") != null) {
                String pricingAction = rootNode.get("pricing_action").asText();
                String strategy = rootNode.has("strategy") ? rootNode.get("strategy").asText() : "";
                
                if (!pricingAction.trim().isEmpty() && !pricingAction.toLowerCase().contains("no pricing action")) {
                    Notification notification = new Notification();
                    notification.setMessage("ACTION REQUIRED: " + pricingAction + "\nSTRATEGY: " + strategy);
                    notification.setIsRead(false);
                    notification.setTimestamp(LocalDateTime.now());
                    
                    // Priority Scoring logic requested in Blueprint
                    int score = 5;
                    String impactLevel = "MEDIUM";
                    
                    if (pricingAction.toLowerCase().contains("reduce") || pricingAction.toLowerCase().contains("$") || pricingAction.toLowerCase().contains("price")) {
                        score = 9;
                        impactLevel = "HIGH";
                    }

                    notification.setScore(score);
                    notification.setImpactLevel(impactLevel);
                    
                    notificationRepository.save(notification);
                    System.out.println("Generated and saved high-impact actionable Notification with Score: " + score);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response for Action Engine: " + e.getMessage());
        }

        return jsonResponse;
    }
}
