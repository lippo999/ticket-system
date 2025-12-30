package com.example.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * Rate limit rule configuration for specific endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRule {
    
    /**
     * Path pattern (e.g., /api/bookings/**, /api/payment/**)
     */
    private String path;
    
    /**
     * HTTP method (GET, POST, etc.) or * for all methods
     */
    private String method;
    
    /**
     * Maximum number of requests allowed
     */
    private int requests;
    
    /**
     * Time window for rate limiting
     */
    private Duration window;
    
    /**
     * Check if this rule matches the given path and method
     */
    public boolean matches(String requestPath, String requestMethod) {
        boolean pathMatches = matchesPath(requestPath);
        boolean methodMatches = method.equals("*") || method.equalsIgnoreCase(requestMethod);
        return pathMatches && methodMatches;
    }
    
    private boolean matchesPath(String requestPath) {
        // Convert path pattern to regex
        // /api/bookings/** -> /api/bookings/.*
        // /api/bookings/* -> /api/bookings/[^/]+
        String regex = path
                .replace("**", ".*")
                .replace("*", "[^/]+")
                .replace("/", "\\/");
        return requestPath.matches(regex);
    }
}
