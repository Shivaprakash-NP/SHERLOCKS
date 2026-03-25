package com.targaryen.marketintel.model;

import jakarta.persistence.*;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long competitorId;
    
    @Column(columnDefinition="TEXT")
    private String content;
    
    private Integer rating;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompetitorId() { return competitorId; }
    public void setCompetitorId(Long competitorId) { this.competitorId = competitorId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
