package com.example.authservice.service.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RedisBase implements GenericRedis {
    protected String prefix;
    protected final RedisTemplate<String, Object> redisTemplate;
    
    protected RedisBase(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public String generateKey(String identifier) {
        return prefix + ":" + identifier;
    }

    @Override
    public boolean saveToRedis(String identifier, String value, long ttlInSeconds) {
        String key = generateKey(identifier);
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlInSeconds));
            return true;
        } catch (Exception e) {
            log.error("Failed to save to Redis. Key: {}, Error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getFromRedis(String identifier) {
        String key = generateKey(identifier);
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                return (String) value;
            }
            log.warn("Expected String but got {} for key: {}", value.getClass().getName(), key);
            return value.toString();
        } catch (Exception e) {
            log.error("Failed to get from Redis. Key: {}, Error: {}", key, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void deleteFromRedis(String identifier) {
        String key = generateKey(identifier);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete from Redis. Key: {}, Error: {}", key, e.getMessage(), e);
        }
    }
}
