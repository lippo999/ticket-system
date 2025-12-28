package com.example.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class JwtParserFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Public endpoints
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/keys/public"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Processing request: {} {}", request.getMethod(), path);

        // Skip JWT parsing cho public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint, skipping JWT parsing");
            return chain.filter(exchange);
        }

        // Extract JWT token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found, continuing without user context");
            return chain.filter(exchange);
        }

        try {
            String token = authHeader.substring(7);
            
            // Parse JWT claims (NO SIGNATURE VALIDATION - just decode base64)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid JWT format");
                return chain.filter(exchange);
            }
            
            // Decode payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode claims = objectMapper.readTree(payload);
            
            // Extract user info
            String userId = claims.has("userId") ? claims.get("userId").asText() : null;
            String role = claims.has("role") ? claims.get("role").asText() : null;
            String username = claims.has("sub") ? claims.get("sub").asText() : null;
            
            log.debug("Parsed JWT - userId: {}, role: {}, username: {}", userId, role, username);

            // Add user info to headers for downstream services
            ServerHttpRequest.Builder requestBuilder = request.mutate();
            
            if (userId != null) {
                requestBuilder.header("X-User-Id", userId);
            }
            if (role != null) {
                requestBuilder.header("X-User-Role", role);
            }
            if (username != null) {
                requestBuilder.header("X-Username", username);
            }
            
            // Forward original Authorization header
            ServerHttpRequest modifiedRequest = requestBuilder.build();
            
            log.info("JWT parsed successfully, forwarding with user context headers");
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            log.error("Failed to parse JWT: {}", e.getMessage());
            // Continue without user context - business service will validate
            return chain.filter(exchange);
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100; // Run before routing
    }
}
