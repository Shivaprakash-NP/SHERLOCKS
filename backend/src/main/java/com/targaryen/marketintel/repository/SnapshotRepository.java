package com.targaryen.marketintel.repository;

import com.targaryen.marketintel.model.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {
}
