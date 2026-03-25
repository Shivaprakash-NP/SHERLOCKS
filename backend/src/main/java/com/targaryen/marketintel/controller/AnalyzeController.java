package com.targaryen.marketintel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.targaryen.marketintel.model.Competitor;
import com.targaryen.marketintel.model.DiffResult;
import com.targaryen.marketintel.model.Notification;
import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.repository.CompetitorRepository;
import com.targaryen.marketintel.repository.NotificationRepository;
import com.targaryen.marketintel.repository.SnapshotRepository;
import com.targaryen.marketintel.service.DiffService;
import com.targaryen.marketintel.service.GeminiService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalyzeController {

    private final GeminiService geminiService;
    private final SnapshotRepository snapshotRepository;
    private final CompetitorRepository competitorRepository;
    private final DiffService diffService;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyzeController(GeminiService geminiService,
                             SnapshotRepository snapshotRepository,
                             CompetitorRepository competitorRepository,
                             DiffService diffService,
                             NotificationRepository notificationRepository) {
        this.geminiService = geminiService;
        this.snapshotRepository = snapshotRepository;
        this.competitorRepository = competitorRepository;
        this.diffService = diffService;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/analyze")
    public String triggerAnalysis() {
        StringBuilder contextBuilder = new StringBuilder();
        
        contextBuilder.append("--- COMPETITOR TIME-SERIES DIFFS / EXTRACTED SIGNALS ---\n");
        List<Competitor> competitors = competitorRepository.findAll();
        for (Competitor c : competitors) {
            List<Snapshot> snaps = snapshotRepository.findAll().stream()
                .filter(s -> s.getCompetitorId().equals(c.getId()))
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(Collectors.toList());

            if (snaps.size() >= 2) {
                Snapshot oldSnap = snaps.get(snaps.size() - 2);
                Snapshot newSnap = snaps.get(snaps.size() - 1);
                DiffResult diff = diffService.extractDiff(oldSnap.getRawContent(), newSnap.getRawContent());
                contextBuilder.append("Competitor: ").append(c.getName()).append("\n");
                contextBuilder.append("Focus specifically on Pricing, Features, and Messaging shifts:\n");
                contextBuilder.append("Changes:\n").append(diff.getNewOrModifiedText()).append("\n\n");
            } else if (snaps.size() == 1) {
                contextBuilder.append("Competitor: ").append(c.getName()).append("\n");
                contextBuilder.append("Initial Data (Analyze for Pricing, Features, Messaging):\n").append(snaps.get(0).getRawContent()).append("\n\n");
            }
        }

        String jsonResponse = geminiService.analyzeMarketData(contextBuilder.toString());

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
                    
                    int score = 5;
                    String impactLevel = "MEDIUM";
                    
                    if (pricingAction.toLowerCase().contains("reduce") || pricingAction.toLowerCase().contains("$") || pricingAction.toLowerCase().contains("price")) {
                        score = 9;
                        impactLevel = "HIGH";
                    }

                    notification.setScore(score);
                    notification.setImpactLevel(impactLevel);
                    
                    notificationRepository.save(notification);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response for Action Engine: " + e.getMessage());
        }

        return jsonResponse;
    }
}
