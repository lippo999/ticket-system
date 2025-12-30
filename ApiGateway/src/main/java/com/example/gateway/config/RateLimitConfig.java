package com.example.gateway.config;

import com.example.gateway.model.RateLimitRule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Rate limit configuration
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public List<RateLimitRule> rateLimitRules() {
        List<RateLimitRule> rules = new ArrayList<>();

        // Default rule for all endpoints: 100 requests per minute
        rules.add(RateLimitRule.builder()
                .path("/**")
                .method("*")
                .requests(100)
                .window(Duration.ofMinutes(1))
                .build());

        // Booking endpoints - stricter limits
        rules.add(RateLimitRule.builder()
                .path("/api/booking/**")
                .method("POST")
                .requests(10)
                .window(Duration.ofMinutes(1))
                .build());

        rules.add(RateLimitRule.builder()
                .path("/api/booking/**")
                .method("GET")
                .requests(50)
                .window(Duration.ofMinutes(1))
                .build());

        // Payment endpoints - very strict
        rules.add(RateLimitRule.builder()
                .path("/api/payment/**")
                .method("POST")
                .requests(5)
                .window(Duration.ofMinutes(1))
                .build());

        // Event endpoints - moderate limits
        rules.add(RateLimitRule.builder()
                .path("/api/event/**")
                .method("GET")
                .requests(50)
                .window(Duration.ofMinutes(1))
                .build());

        rules.add(RateLimitRule.builder()
                .path("/api/event/**")
                .method("POST")
                .requests(20)
                .window(Duration.ofMinutes(1))
                .build());

        // Auth endpoints - prevent brute force
        rules.add(RateLimitRule.builder()
                .path("/api/auth/login")
                .method("POST")
                .requests(5)
                .window(Duration.ofMinutes(5))
                .build());

        rules.add(RateLimitRule.builder()
                .path("/api/auth/register")
                .method("POST")
                .requests(3)
                .window(Duration.ofMinutes(10))
                .build());

        return rules;
    }
}
