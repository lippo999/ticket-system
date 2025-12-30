package com.example.gateway.service;

import com.example.gateway.model.RateLimitResult;
import reactor.core.publisher.Mono;

/**
 * Rate limiter interface
 */
public interface RateLimiter {
    
    /**
     * Check if a request is allowed based on rate limit rules
     * 
     * @param key Unique identifier for the rate limit (e.g., userId:endpoint)
     * @param maxRequests Maximum number of requests allowed
     * @param windowSeconds Time window in seconds
     * @return RateLimitResult indicating if request is allowed
     */
    Mono<RateLimitResult> isAllowed(String key, int maxRequests, long windowSeconds);
}
