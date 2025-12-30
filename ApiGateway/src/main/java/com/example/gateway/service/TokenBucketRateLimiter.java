package com.example.gateway.service;

import com.example.gateway.model.RateLimitResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Token Bucket algorithm implementation using Redis
 * 
 * Token Bucket works as follows:
 * 1. Each user has a bucket with a certain number of tokens
 * 2. Each request consumes 1 token
 * 3. Tokens are refilled at a constant rate
 * 4. If bucket is empty, request is denied
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiter {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REDIS_KEY_PREFIX = "rate_limit:";

    @Override
    public Mono<RateLimitResult> isAllowed(String key, int maxRequests, long windowSeconds) {
        String redisKey = REDIS_KEY_PREFIX + key;
        long now = Instant.now().getEpochSecond();

        return redisTemplate.opsForValue().get(redisKey)
                .defaultIfEmpty("{}")
                .flatMap(value -> {
                    try {
                        BucketState state = parseState(value, maxRequests, now);
                        
                        // Refill tokens based on time elapsed
                        long elapsed = now - state.lastRefill;
                        double refillRate = (double) maxRequests / windowSeconds;
                        double tokensToAdd = elapsed * refillRate;
                        double newTokens = Math.min(state.tokens + tokensToAdd, maxRequests);

                        log.debug("Rate limit check - key: {}, tokens: {}, elapsed: {}s, tokensToAdd: {}, newTokens: {}", 
                                key, state.tokens, elapsed, tokensToAdd, newTokens);

                        if (newTokens >= 1) {
                            // Allow request - consume 1 token
                            double remainingTokens = newTokens - 1;
                            BucketState newState = new BucketState(remainingTokens, now);
                            
                            return saveState(redisKey, newState, windowSeconds)
                                    .thenReturn(RateLimitResult.allowed(
                                            maxRequests,
                                            (int) Math.floor(remainingTokens),
                                            now + windowSeconds
                                    ));
                        } else {
                            // Deny request - no tokens available
                            long resetAt = now + windowSeconds;
                            long retryAfter = (long) Math.ceil((1 - newTokens) / refillRate);
                            
                            log.warn("Rate limit exceeded - key: {}, tokens: {}, retryAfter: {}s", 
                                    key, newTokens, retryAfter);
                            
                            return Mono.just(RateLimitResult.denied(
                                    maxRequests,
                                    resetAt,
                                    retryAfter
                            ));
                        }
                    } catch (Exception e) {
                        log.error("Error processing rate limit: {}", e.getMessage());
                        // On error, allow the request (fail-open)
                        return Mono.just(RateLimitResult.allowed(maxRequests, maxRequests, now + windowSeconds));
                    }
                });
    }

    private BucketState parseState(String json, int maxRequests, long now) {
        try {
            if (json == null || json.equals("{}")) {
                // Initialize new bucket with full tokens
                return new BucketState(maxRequests, now);
            }
            
            JsonNode node = objectMapper.readTree(json);
            double tokens = node.has("tokens") ? node.get("tokens").asDouble() : maxRequests;
            long lastRefill = node.has("lastRefill") ? node.get("lastRefill").asLong() : now;
            
            return new BucketState(tokens, lastRefill);
        } catch (Exception e) {
            log.error("Error parsing bucket state: {}", e.getMessage());
            return new BucketState(maxRequests, now);
        }
    }

    private Mono<Boolean> saveState(String key, BucketState state, long ttlSeconds) {
        try {
            String json = String.format("{\"tokens\":%.2f,\"lastRefill\":%d}", 
                    state.tokens, state.lastRefill);
            return redisTemplate.opsForValue()
                    .set(key, json, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Error saving bucket state: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    /**
     * Internal state of a token bucket
     */
    private static class BucketState {
        final double tokens;
        final long lastRefill;

        BucketState(double tokens, long lastRefill) {
            this.tokens = tokens;
            this.lastRefill = lastRefill;
        }
    }
}
