package com.example.authservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/keys")
public class KeyController {

    @Value("${jwt.public-key}")
    private String publicKey;

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", publicKey);
        response.put("algorithm", "RS256");
        return ResponseEntity.ok(response);
    }
}
