package com.example.authservice.service.redis;

public interface GenericRedis {
    String generateKey(String identifier);
    boolean saveToRedis(String key, String value, long ttlInSeconds);
    String getFromRedis(String key);
    void deleteFromRedis(String key);
}
