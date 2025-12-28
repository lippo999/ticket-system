package com.example.booking.controller;

import com.example.booking.security.JwtValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final JwtValidator jwtValidator;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestHeader(value = "X-User-Id", required = false) String gatewayUserId,
            @RequestHeader(value = "X-User-Role", required = false) String gatewayRole
    ) {
        Map<String, Object> response = new HashMap<>();
        
        // Headers from Gateway
        response.put("gatewayParsedUserId", gatewayUserId);
        response.put("gatewayParsedRole", gatewayRole);
        
        // Validate JWT in business service
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Claims claims = jwtValidator.validateToken(token);
                
                response.put("jwtValidated", true);
                response.put("validatedUserId", jwtValidator.extractUserId(claims));
                response.put("validatedUsername", jwtValidator.extractUsername(claims));
                response.put("validatedRole", jwtValidator.extractRole(claims));
                response.put("tokenExpired", jwtValidator.isTokenExpired(claims));
                
                log.info("JWT validated successfully for user: {}", claims.getSubject());
                
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                response.put("jwtValidated", false);
                response.put("error", e.getMessage());
                return ResponseEntity.status(401).body(response);
            }
        } else {
            response.put("jwtValidated", false);
            response.put("error", "No JWT token provided");
            return ResponseEntity.status(401).body(response);
        }
        
        response.put("message", "Booking Service - JWT Validation Test");
        return ResponseEntity.ok(response);
    }
}
