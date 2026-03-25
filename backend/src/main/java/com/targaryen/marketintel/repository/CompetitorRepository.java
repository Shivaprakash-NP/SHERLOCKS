package com.targaryen.marketintel.repository;

import com.targaryen.marketintel.model.Competitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long> {
}
