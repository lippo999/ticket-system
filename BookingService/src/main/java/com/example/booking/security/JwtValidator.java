package com.example.booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtValidator {

    private final PublicKeyService publicKeyService;

    /**
     * Validate JWT token signature and return claims
     * @param token JWT token string (without "Bearer " prefix)
     * @return Claims if valid
     * @throws RuntimeException if validation fails
     */
    public Claims validateToken(String token) {
        PublicKey publicKey = publicKeyService.getPublicKey();
        
        if (publicKey == null) {
            log.error("Public key not available for JWT validation");
            throw new RuntimeException("Public key not available");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            log.debug("JWT validated successfully for user: {}", claims.getSubject());
            return claims;
            
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Extract username from claims
     */
    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extract user ID from claims
     */
    public String extractUserId(Claims claims) {
        return claims.get("userId", String.class);
    }

    /**
     * Extract role from claims
     */
    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new java.util.Date());
    }
}
