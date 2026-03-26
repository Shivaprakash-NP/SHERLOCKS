package com.targaryen.marketintel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.targaryen.marketintel.model.Competitor;
import com.targaryen.marketintel.model.Notification;
import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.repository.CompetitorRepository;
import com.targaryen.marketintel.repository.NotificationRepository;
import com.targaryen.marketintel.repository.SnapshotRepository;
import com.targaryen.marketintel.util.HashUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketSyncService {

    private final CompetitorRepository competitorRepository;
    private final SnapshotRepository snapshotRepository;
    private final ScrapingClientService scrapingClientService;
    private final HashUtil hashUtil;
    
    // NEW: Injecting the AI Brain directly into the Background Scanner
    private final GeminiService geminiService;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MarketSyncService(CompetitorRepository competitorRepository,
                             SnapshotRepository snapshotRepository,
                             ScrapingClientService scrapingClientService,
                             HashUtil hashUtil,
                             GeminiService geminiService,
                             NotificationRepository notificationRepository) {
        this.competitorRepository = competitorRepository;
        this.snapshotRepository = snapshotRepository;
        this.scrapingClientService = scrapingClientService;
        this.hashUtil = hashUtil;
        this.geminiService = geminiService;
        this.notificationRepository = notificationRepository;
    }

    @Async
    public void syncSingleCompetitorAsync(Competitor competitor) {
        System.out.println("Triggering immediate async scrape for: " + competitor.getName());
        syncCompetitor(competitor);
    }

    private void syncCompetitor(Competitor competitor) {
        String rawContent = scrapingClientService.scrape(competitor.getUrl());
        if (rawContent == null || rawContent.equals("Failed to scrape content.")) {
            return;
        }

        String newHash = hashUtil.generateHash(rawContent);

        Snapshot lastSnapshot = snapshotRepository.findAll().stream()
                .filter(s -> s.getCompetitorId().equals(competitor.getId()))
                .reduce((first, second) -> second)
                .orElse(null);

        // If the hash changed, the website physically changed
        if (lastSnapshot == null || !newHash.equals(lastSnapshot.getContentHash())) {
            System.out.println("CRITICAL DOM CHANGE DETECTED FOR: " + competitor.getName() + " - Waking up AI...");

            Snapshot newSnapshot = new Snapshot();
            newSnapshot.setCompetitorId(competitor.getId());
            newSnapshot.setRawContent(rawContent);
            newSnapshot.setContentHash(newHash);
            newSnapshot.setTimestamp(LocalDateTime.now());
            snapshotRepository.save(newSnapshot);

            // NEW: Autonomous AI Trigger
            // Only trigger the AI if we have an older snapshot to compare the new one against
            if (lastSnapshot != null) {
                analyzeChangeAutonomously(competitor, lastSnapshot.getRawContent(), rawContent);
            }
        } else {
            System.out.println("Pulse Check: No structural changes for " + competitor.getName());
        }
    }

    // NEW: The Background AI Evaluator
    private void analyzeChangeAutonomously(Competitor c, String oldContent, String newContent) {
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Competitor: ").append(c.getName()).append("\n");
        contextBuilder.append("[PAST SNAPSHOT]:\n").append(oldContent).append("\n\n");
        contextBuilder.append("[CURRENT SNAPSHOT]:\n").append(newContent).append("\n\n");

        String jsonResponse = geminiService.analyzeMarketData(contextBuilder.toString());

        // BULLETPROOF JSON EXTRACTOR
        String cleanJsonResponse = jsonResponse;
        int startIndex = cleanJsonResponse.indexOf('{');
        int endIndex = cleanJsonResponse.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1) {
            cleanJsonResponse = cleanJsonResponse.substring(startIndex, endIndex + 1);
        } else {
            return; // Abort if no JSON is found
        }

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
                        notification.setMessage("Live Shift [" + category + "] at " + c.getName() + ": Changed from '" + oldState + "' to '" + newState + "'");
                        notification.setIsRead(false);
                        notification.setTimestamp(LocalDateTime.now());
                        notification.setScore(category.equals("PRICING") || category.equals("PRODUCT_LAUNCH") ? 10 : 7);
                        notification.setImpactLevel(category.equals("PRICING") || category.equals("PRODUCT_LAUNCH") ? "CRITICAL" : "HIGH");
                        notificationRepository.save(notification);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Autonomous AI Parse Failed: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 10000)
    public void syncMarketData() {
        List<Competitor> competitors = competitorRepository.findAll();
        if (competitors.isEmpty()) return;
        
        System.out.println("Starting Global Market Sync Cron (10s Pulse)...");
        for (Competitor competitor : competitors) {
            syncCompetitor(competitor);
        }
    }
}