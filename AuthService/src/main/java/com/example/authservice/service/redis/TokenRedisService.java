package com.example.authservice.service.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenRedisService extends RedisBase {
    
    public TokenRedisService(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
        this.prefix = "token";
    }
}
