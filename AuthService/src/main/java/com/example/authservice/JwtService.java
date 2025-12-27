package com.example.authservice;

import org.springframework.stereotype.Service;

import com.example.authservice.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUsername());
        claims.put("role", user.getRole().getName());
        claims.put("permissions", user.getPermissions().stream().map(p -> p.getName()).toList());
        claims.put("name", user.getFullName());
        return Jwts.builder().claims().add(claims)
                .subject(user.getUsername())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + jwtExpirationInMs))
                .and()
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
    }

    public Claims validateTokenAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractInfoFromToken(String token, String infoKey) {
        Claims claims = validateTokenAndGetClaims(token);
        return claims.get(infoKey, String.class);
    }
}
