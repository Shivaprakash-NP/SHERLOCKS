package com.targaryen.marketintel.model;

import jakarta.persistence.*;

@Entity
public class Competitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;

    public Competitor() {}

    public Competitor(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
