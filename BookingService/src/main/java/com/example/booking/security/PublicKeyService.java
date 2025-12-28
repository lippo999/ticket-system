package com.example.booking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class PublicKeyService {

    @Value("${auth.service.url:http://localhost:8001}")
    private String authServiceUrl;

    private PublicKey cachedPublicKey;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        log.info("Initializing Public Key Service");
        fetchPublicKey();
    }

    /**
     * Refresh public key every 48 hours
     */
    @Scheduled(fixedRate = 172800000) // 48 hours = 48 * 60 * 60 * 1000 ms
    public void refreshPublicKey() {
        log.info("Scheduled public key refresh triggered");
        fetchPublicKey();
    }

    /**
     * Fetch public key from AuthService
     * Falls back to cached key if AuthService is unavailable
     */
    public void fetchPublicKey() {
        try {
            String url = authServiceUrl + "/api/keys/public";
            log.info("Fetching public key from: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, String> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("publicKey")) {
                String publicKeyPem = response.get("publicKey");
                PublicKey newKey = parsePublicKey(publicKeyPem);
                
                // Update cached key
                this.cachedPublicKey = newKey;
                log.info("Public key fetched and cached successfully");
            } else {
                log.error("Invalid response from AuthService: {}", response);
                logFallbackStatus();
            }
        } catch (Exception e) {
            log.error("Failed to fetch public key from AuthService: {}", e.getMessage());
            logFallbackStatus();
        }
    }

    private void logFallbackStatus() {
        if (cachedPublicKey != null) {
            log.warn("Using cached public key as fallback");
        } else {
            log.error("No cached public key available! JWT validation will fail!");
        }
    }

    /**
     * Parse PEM format public key string to PublicKey object
     */
    private PublicKey parsePublicKey(String publicKeyPem) throws Exception {
        String key = publicKeyPem
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * Get cached public key
     * @return PublicKey or null if not available
     */
    public PublicKey getPublicKey() {
        if (cachedPublicKey == null) {
            log.warn("Public key not available, attempting to fetch...");
            fetchPublicKey();
        }
        return cachedPublicKey;
    }

    /**
     * Check if public key is available
     */
    public boolean isKeyAvailable() {
        return cachedPublicKey != null;
    }
}
