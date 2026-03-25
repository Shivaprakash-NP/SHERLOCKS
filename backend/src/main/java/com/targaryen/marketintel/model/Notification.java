package com.targaryen.marketintel.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition="TEXT")
    private String message;
    private Boolean isRead;
    private LocalDateTime timestamp;
    private Integer score;
    private String impactLevel;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getImpactLevel() { return impactLevel; }
    public void setImpactLevel(String impactLevel) { this.impactLevel = impactLevel; }
}
