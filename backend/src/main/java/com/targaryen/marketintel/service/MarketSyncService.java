package com.targaryen.marketintel.service;

import com.targaryen.marketintel.model.Competitor;
import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.model.DiffResult;
import com.targaryen.marketintel.repository.CompetitorRepository;
import com.targaryen.marketintel.repository.SnapshotRepository;
import com.targaryen.marketintel.util.HashUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketSyncService {

    private final CompetitorRepository competitorRepository;
    private final SnapshotRepository snapshotRepository;
    private final ScrapingClientService scrapingClientService;
    private final HashUtil hashUtil;
    private final DiffService diffService;

    public MarketSyncService(CompetitorRepository competitorRepository,
                             SnapshotRepository snapshotRepository,
                             ScrapingClientService scrapingClientService,
                             HashUtil hashUtil,
                             DiffService diffService) {
        this.competitorRepository = competitorRepository;
        this.snapshotRepository = snapshotRepository;
        this.scrapingClientService = scrapingClientService;
        this.hashUtil = hashUtil;
        this.diffService = diffService;
    }

    // Cron job running every 60 seconds (accelerated for hackathon demo)
    @Scheduled(fixedRate = 60000)
    public void syncMarketData() {
        System.out.println("Starting Market Sync...");
        List<Competitor> competitors = competitorRepository.findAll();

        for (Competitor competitor : competitors) {
            String rawContent = scrapingClientService.scrape(competitor.getUrl());
            if (rawContent == null || rawContent.equals("Failed to scrape content.")) {
                System.out.println("Skipped sync for " + competitor.getName() + " due to scraping failure.");
                continue;
            }
            
            String newHash = hashUtil.generateHash(rawContent);

            // Fetch last snapshot
            Snapshot lastSnapshot = snapshotRepository.findAll().stream()
                    .filter(s -> s.getCompetitorId().equals(competitor.getId()))
                    .reduce((first, second) -> second)
                    .orElse(null);

            if (lastSnapshot == null || !newHash.equals(lastSnapshot.getContentHash())) {
                System.out.println("Change detected for: " + competitor.getName());

                // Trigger Diff Extraction (Phase 4)
                String oldContent = (lastSnapshot != null) ? lastSnapshot.getRawContent() : null;
                DiffResult diff = diffService.extractDiff(oldContent, rawContent);
                
                System.out.println("=== DIFF EXTRACTION RESULT ===");
                System.out.println(diff.getNewOrModifiedText());
                System.out.println("==============================");

                // Save new snapshot
                Snapshot newSnapshot = new Snapshot();
                newSnapshot.setCompetitorId(competitor.getId());
                newSnapshot.setRawContent(rawContent);
                newSnapshot.setContentHash(newHash);
                newSnapshot.setTimestamp(LocalDateTime.now());
                snapshotRepository.save(newSnapshot);

                System.out.println("Saved new snapshot.");
            } else {
                System.out.println("No changes detected for: " + competitor.getName());
            }
        }
    }
}
