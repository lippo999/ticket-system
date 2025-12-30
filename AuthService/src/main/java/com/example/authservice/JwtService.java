package com.example.authservice;

import org.springframework.stereotype.Service;

import com.example.authservice.entity.User;
import com.example.authservice.service.redis.TokenRedisService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {
    @Value("${jwt.private-key}")
    private String privateKeyString;
    
    @Value("${jwt.public-key}")
    private String publicKeyString;

    @Value("${jwt.expiration}")
    private Long jwtExpirationInMs;

    @Autowired
    private TokenRedisService tokenRedisService;

    private PrivateKey getPrivateKey() {
        try {
            String key = privateKeyString
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error loading private key", e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            String key = publicKeyString
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error loading public key", e);
        }
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUsername());
        claims.put("role", user.getRole().getName());
        claims.put("permissions", user.getPermissions().stream().map(p -> p.getName()).toList());
        claims.put("name", user.getFullName());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .id(UUID.randomUUID().toString())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(getPrivateKey())
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims validateTokenAndGetClaims(String token) {
        Claims claims = parseToken(token);
        
        // Check expiration
        Date expiration = claims.getExpiration();
        if (expiration.before(new Date())) {
            throw new RuntimeException("Token has expired");
        }
        
        // Check blacklist
        String jti = claims.getId();
        if (jti != null && isJtiBlacklisted(jti)) {
            throw new RuntimeException("Token is blacklisted");
        }
        
        return claims;
    }

    public String extractInfoFromToken(String token, String infoKey) {
        Claims claims = validateTokenAndGetClaims(token);
        return claims.get(infoKey, String.class);
    }

    public String getJTIFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    public void addToBlacklist(String jti) {
        long ttlInSeconds = jwtExpirationInMs / 1000;
        tokenRedisService.saveToRedis(jti, "blacklisted", ttlInSeconds);
    }

    public boolean isJtiBlacklisted(String jti) {
        String value = tokenRedisService.getFromRedis(jti);
        return value != null;
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            String jti = getJTIFromToken(token);
            return isJtiBlacklisted(jti);
        } catch (Exception e) {
            return true;
        }
    }
}
