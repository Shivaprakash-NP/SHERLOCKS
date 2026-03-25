package com.targaryen.marketintel.controller;

import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.repository.ProductOfferRepository;
import com.targaryen.marketintel.repository.ReviewRepository;
import com.targaryen.marketintel.repository.SnapshotRepository;
import com.targaryen.marketintel.service.GeminiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final GeminiService geminiService;
    private final SnapshotRepository snapshotRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ReviewRepository reviewRepository;

    public AnalyzeController(GeminiService geminiService,
                             SnapshotRepository snapshotRepository,
                             ProductOfferRepository productOfferRepository,
                             ReviewRepository reviewRepository) {
        this.geminiService = geminiService;
        this.snapshotRepository = snapshotRepository;
        this.productOfferRepository = productOfferRepository;
        this.reviewRepository = reviewRepository;
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

        return geminiService.analyzeMarketData(contextBuilder.toString());
    }
}
