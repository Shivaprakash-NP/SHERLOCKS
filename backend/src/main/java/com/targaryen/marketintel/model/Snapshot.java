package com.targaryen.marketintel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Snapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long competitorId;
    private String contentHash;
    
    @Column(columnDefinition="TEXT")
    private String rawContent;
    
    private LocalDateTime timestamp;

    public Snapshot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompetitorId() { return competitorId; }
    public void setCompetitorId(Long competitorId) { this.competitorId = competitorId; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
