package com.example.gateway.exception;

import com.example.gateway.model.RateLimitResult;
import lombok.Getter;

/**
 * Exception thrown when rate limit is exceeded
 */
@Getter
public class RateLimitExceededException extends RuntimeException {
    
    private final RateLimitResult rateLimitResult;
    
    public RateLimitExceededException(RateLimitResult rateLimitResult) {
        super("Rate limit exceeded. Retry after " + rateLimitResult.getRetryAfter() + " seconds");
        this.rateLimitResult = rateLimitResult;
    }
}
