package com.example.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "AuthService");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("port", 8001);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login endpoint");
        response.put("username", credentials.get("username"));
        response.put("token", "dummy-jwt-token-12345");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> userInfo) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Register endpoint");
        response.put("username", userInfo.get("username"));
        response.put("email", userInfo.get("email"));
        response.put("userId", "user-" + System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Token validation endpoint");
        response.put("token", token);
        response.put("valid", token != null && !token.isEmpty());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AuthService is working! Port: 8001");
    }
}

