package com.example.RateLimiting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private int capacity = 10;  //a.k.a burst capacity
    private long refillRate = 5;
    private String apiServerUrl = "http://localhost:8080";

    private long timeout = 5000;
}
