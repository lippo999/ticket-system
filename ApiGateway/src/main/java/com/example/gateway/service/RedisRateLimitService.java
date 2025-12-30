package com.example.gateway.service;

import com.example.gateway.model.RateLimitResult;
import com.example.gateway.model.RateLimitRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service to handle rate limiting logic
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimitService {

    private final RateLimiter rateLimiter;
    private final List<RateLimitRule> rateLimitRules;

    /**
     * Check rate limit for a request
     * 
     * @param identifier User identifier (userId or IP address)
     * @param path Request path
     * @param method HTTP method
     * @return RateLimitResult
     */
    public Mono<RateLimitResult> checkRateLimit(String identifier, String path, String method) {
        // Find matching rule
        RateLimitRule rule = findMatchingRule(path, method);
        
        if (rule == null) {
            log.debug("No rate limit rule found for {} {}", method, path);
            // No rule found, allow by default
            return Mono.just(RateLimitResult.allowed(Integer.MAX_VALUE, Integer.MAX_VALUE, 0));
        }

        // Build Redis key: identifier:method:path
        String key = buildKey(identifier, method, path);
        
        log.debug("Checking rate limit - key: {}, rule: {} requests per {}s", 
                key, rule.getRequests(), rule.getWindow().getSeconds());

        // Check rate limit using token bucket algorithm
        return rateLimiter.isAllowed(
                key,
                rule.getRequests(),
                rule.getWindow().getSeconds()
        );
    }

    /**
     * Find the first matching rule for the given path and method
     */
    private RateLimitRule findMatchingRule(String path, String method) {
        return rateLimitRules.stream()
                .filter(rule -> rule.matches(path, method))
                .findFirst()
                .orElse(null);
    }

    /**
     * Build Redis key for rate limiting
     * Format: identifier:path_simplified
     */
    private String buildKey(String identifier, String method, String path) {
        // Simplify path: /api/bookings/123 -> /api/bookings/:id
        String simplifiedPath = simplifyPath(path);
        return String.format("%s:%s", identifier, simplifiedPath);
    }

    /**
     * Simplify path by replacing IDs with placeholders
     * Example: /api/bookings/123 -> /api/bookings/:id
     */
    private String simplifyPath(String path) {
        // Replace UUID-like patterns
        path = path.replaceAll("/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "/:id");
        // Replace numeric IDs
        path = path.replaceAll("/\\d+", "/:id");
        return path;
    }
}
