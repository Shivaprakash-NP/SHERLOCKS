package com.targaryen.marketintel.controller;

import com.targaryen.marketintel.model.Competitor;
import com.targaryen.marketintel.repository.CompetitorRepository;
import com.targaryen.marketintel.service.MarketSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URL;

import java.util.List;

@RestController
@RequestMapping("/api/competitors")
@CrossOrigin(origins = "*")
public class CompetitorController {

    private final CompetitorRepository competitorRepository;
    private final MarketSyncService marketSyncService;

    public CompetitorController(CompetitorRepository competitorRepository, MarketSyncService marketSyncService) {
        this.competitorRepository = competitorRepository;
        this.marketSyncService = marketSyncService;
    }

    @GetMapping
    public List<Competitor> getAllCompetitors() {
        return competitorRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addCompetitor(@RequestBody Competitor competitor) {
        // Validation Constraint: Strictly check valid URL formatting
        try {
            new URL(competitor.getUrl()).toURI();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid URL format. Please include protocol (http:// or https://)\"}");
        }

        if (competitor.getName() == null || competitor.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Competitor designation name is critically required.\"}");
        }

        Competitor savedCompetitor = competitorRepository.save(competitor);

        // Core Requirement: Immediately trigger async scraping job unblocked from client thread
        marketSyncService.syncSingleCompetitorAsync(savedCompetitor);

        return ResponseEntity.ok(savedCompetitor);
    }
}
