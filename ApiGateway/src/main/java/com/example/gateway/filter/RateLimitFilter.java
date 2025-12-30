package com.example.gateway.filter;

import com.example.gateway.model.RateLimitResult;
import com.example.gateway.service.RedisRateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Rate limiting filter using Redis
 * Applies after JWT parsing to get user ID
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final RedisRateLimitService rateLimitService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // Get identifier: userId from header (set by JwtParserFilter) or IP address
        String userId = request.getHeaders().getFirst("X-User-Id");
        String identifier = userId != null ? "user:" + userId : "ip:" + getClientIp(request);

        log.debug("Rate limit check for {} {} - identifier: {}", method, path, identifier);

        // Check rate limit
        return rateLimitService.checkRateLimit(identifier, path, method)
                .flatMap(result -> {
                    // Add rate limit headers
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("X-RateLimit-Limit", String.valueOf(result.getLimit()));
                    response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
                    response.getHeaders().add("X-RateLimit-Reset", String.valueOf(result.getResetAt()));

                    if (result.isAllowed()) {
                        log.debug("Request allowed - remaining: {}/{}", result.getRemaining(), result.getLimit());
                        return chain.filter(exchange);
                    } else {
                        // Rate limit exceeded
                        log.warn("Rate limit exceeded for {} - identifier: {}, retryAfter: {}s", 
                                path, identifier, result.getRetryAfter());
                        
                        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        response.getHeaders().add("Retry-After", String.valueOf(result.getRetryAfter()));
                        
                        // Return JSON error response
                        String errorBody = String.format(
                                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please retry after %d seconds\",\"retryAfter\":%d}",
                                result.getRetryAfter(), result.getRetryAfter()
                        );
                        
                        return response.writeWith(Mono.just(
                                response.bufferFactory().wrap(errorBody.getBytes())
                        ));
                    }
                })
                .onErrorResume(e -> {
                    // On error, log and allow request (fail-open)
                    log.error("Error checking rate limit: {}", e.getMessage());
                    return chain.filter(exchange);
                });
    }

    /**
     * Get client IP address from request (IPv4 format)
     */
    private String getClientIp(ServerHttpRequest request) {
        // Check X-Forwarded-For header first (for proxied requests)
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            String ip = xff.split(",")[0].trim();
            return convertToIPv4(ip);
        }

        // Fall back to remote address
        if (request.getRemoteAddress() != null) {
            String ip = request.getRemoteAddress().getAddress().getHostAddress();
            return convertToIPv4(ip);
        }

        return "unknown";
    }

    /**
     * Convert IPv6 localhost to IPv4 format
     */
    private String convertToIPv4(String ip) {
        // Convert IPv6 localhost (0:0:0:0:0:0:0:1) to IPv4 (127.0.0.1)
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "127.0.0.1";
        }
        return ip;
    }

    @Override
    public int getOrder() {
        // Run after JwtParserFilter (-100) but before routing
        return -50;
    }
}
