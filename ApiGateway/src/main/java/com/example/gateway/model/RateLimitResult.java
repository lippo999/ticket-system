package com.example.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of rate limit check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitResult {
    
    /**
     * Whether the request is allowed
     */
    private boolean allowed;
    
    /**
     * Total number of requests allowed in the window
     */
    private int limit;
    
    /**
     * Number of requests remaining
     */
    private int remaining;
    
    /**
     * Timestamp when the rate limit will reset (in seconds since epoch)
     */
    private long resetAt;
    
    /**
     * Number of seconds to wait before retrying (only if not allowed)
     */
    private Long retryAfter;
    
    /**
     * Create a result for allowed request
     */
    public static RateLimitResult allowed(int limit, int remaining, long resetAt) {
        return RateLimitResult.builder()
                .allowed(true)
                .limit(limit)
                .remaining(remaining)
                .resetAt(resetAt)
                .build();
    }
    
    /**
     * Create a result for denied request
     */
    public static RateLimitResult denied(int limit, long resetAt, long retryAfter) {
        return RateLimitResult.builder()
                .allowed(false)
                .limit(limit)
                .remaining(0)
                .resetAt(resetAt)
                .retryAfter(retryAfter)
                .build();
    }
}
