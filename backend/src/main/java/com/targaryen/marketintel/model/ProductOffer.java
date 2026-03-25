package com.targaryen.marketintel.model;

import jakarta.persistence.*;

@Entity
public class ProductOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double currentPrice;
    private Double previousPrice;
    private String competitorPrice;

    public ProductOffer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public Double getPreviousPrice() { return previousPrice; }
    public void setPreviousPrice(Double previousPrice) { this.previousPrice = previousPrice; }
    public String getCompetitorPrice() { return competitorPrice; }
    public void setCompetitorPrice(String competitorPrice) { this.competitorPrice = competitorPrice; }
}
