package com.targaryen.marketintel.util;

import com.targaryen.marketintel.model.*;
import com.targaryen.marketintel.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompetitorRepository competitorRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ReviewRepository reviewRepository;

    public DataSeeder(CompetitorRepository competitorRepository,
                      ProductOfferRepository productOfferRepository,
                      ReviewRepository reviewRepository) {
        this.competitorRepository = competitorRepository;
        this.productOfferRepository = productOfferRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (competitorRepository.count() == 0) {
            Competitor comp1 = new Competitor("Tech Giant A", "https://techgianta.com/pricing");
            competitorRepository.save(comp1);

            ProductOffer offer1 = new ProductOffer();
            offer1.setName("Enterprise Cloud Suite");
            offer1.setCurrentPrice(299.99);
            offer1.setPreviousPrice(349.99);
            offer1.setCompetitorPrice("299.99");
            productOfferRepository.save(offer1);

            Review rev1 = new Review();
            rev1.setCompetitorId(comp1.getId());
            rev1.setContent("Pricing is good but support is lacking.");
            rev1.setRating(3);
            reviewRepository.save(rev1);

            System.out.println("Data seeding completed with mock competitors, product offers, and reviews.");
        }
    }
}
