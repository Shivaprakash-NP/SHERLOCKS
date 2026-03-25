package com.targaryen.marketintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarketintelApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketintelApplication.class, args);
    }
}
