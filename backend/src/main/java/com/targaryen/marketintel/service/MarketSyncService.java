package com.targaryen.marketintel.service;

import com.targaryen.marketintel.model.Competitor;
import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.model.DiffResult;
import com.targaryen.marketintel.repository.CompetitorRepository;
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

    @Async
    public void syncSingleCompetitorAsync(Competitor competitor) {
        System.out.println("Triggering immediate async scrape algorithm for newly boarded URL: " + competitor.getName());
        syncCompetitor(competitor);
    }

    private void syncCompetitor(Competitor competitor) {
        String rawContent = scrapingClientService.scrape(competitor.getUrl());
        if (rawContent == null || rawContent.equals("Failed to scrape content.")) {
            System.out.println("Skipped sync for " + competitor.getName() + " due to scraping failure.");
            return;
        }
        
        String newHash = hashUtil.generateHash(rawContent);

        Snapshot lastSnapshot = snapshotRepository.findAll().stream()
                .filter(s -> s.getCompetitorId().equals(competitor.getId()))
                .reduce((first, second) -> second)
                .orElse(null);

        if (lastSnapshot == null || !newHash.equals(lastSnapshot.getContentHash())) {
            System.out.println("Change detected for: " + competitor.getName());

            String oldContent = (lastSnapshot != null) ? lastSnapshot.getRawContent() : null;
            DiffResult diff = diffService.extractDiff(oldContent, rawContent);
            
            System.out.println("=== DIFF EXTRACTION RESULT ===");
            System.out.println("Extracted " + diff.getNewOrModifiedText().length() + " bytes of newly isolated text.");
            System.out.println("==============================");

            Snapshot newSnapshot = new Snapshot();
            newSnapshot.setCompetitorId(competitor.getId());
            newSnapshot.setRawContent(rawContent);
            newSnapshot.setContentHash(newHash);
            newSnapshot.setTimestamp(LocalDateTime.now());
            snapshotRepository.save(newSnapshot);

            System.out.println("Saved highly current snapshot.");
        } else {
            System.out.println("No structural changes detected for: " + competitor.getName());
        }
    }

    @Scheduled(fixedRate = 60000)
    public void syncMarketData() {
        List<Competitor> competitors = competitorRepository.findAll();
        if (competitors.isEmpty()) {
            System.out.println("Market Sync Cron: Sleeping. 0 competitors actively scanned.");
            return;
        }
        
        System.out.println("Starting Global Market Sync Cron...");
        for (Competitor competitor : competitors) {
            syncCompetitor(competitor);
        }
    }
}
