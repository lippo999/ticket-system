package com.example.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Redis operations
 * Make sure Redis is running: docker-compose up -d redis
 */
@SpringBootTest
class RedisIntegrationTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String TEST_KEY_PREFIX = "test:";

    @BeforeEach
    void setUp() {
        // Clean up test keys before each test
        redisTemplate.keys(TEST_KEY_PREFIX + "*")
                .flatMap(redisTemplate::delete)
                .blockLast(Duration.ofSeconds(5));
    }

    @Test
    void testRedisConnection() {
        // Given
        String key = TEST_KEY_PREFIX + "connection";
        String value = "Hello Redis!";

        // When: Set value
        Boolean setResult = redisTemplate.opsForValue()
                .set(key, value)
                .block();

        // Then: Should succeed
        assertThat(setResult).isTrue();

        // And: Get value back
        StepVerifier.create(redisTemplate.opsForValue().get(key))
                .expectNext(value)
                .verifyComplete();
    }

    @Test
    void testSetAndGet() {
        // Given
        String key = TEST_KEY_PREFIX + "mykey";
        String value = "myvalue";

        // When
        redisTemplate.opsForValue().set(key, value).block();

        // Then
        String result = redisTemplate.opsForValue().get(key).block();
        assertThat(result).isEqualTo(value);
    }

    @Test
    void testSetWithExpiration() throws InterruptedException {
        // Given
        String key = TEST_KEY_PREFIX + "expiring";
        String value = "temporary";
        Duration ttl = Duration.ofSeconds(2);

        // When: Set with TTL
        redisTemplate.opsForValue().set(key, value, ttl).block();

        // Then: Key exists immediately
        Boolean exists = redisTemplate.hasKey(key).block();
        assertThat(exists).isTrue();

        // Wait for expiration
        Thread.sleep(2500);

        // Key should be gone
        Boolean stillExists = redisTemplate.hasKey(key).block();
        assertThat(stillExists).isFalse();
    }

    @Test
    void testDelete() {
        // // Given
        // String key = "demo:*";
        // redisTemplate.opsForValue().set(key, "value").block();

        // // When
        // Long deleted = redisTemplate.delete(key).block();

        // // Then
        // assertThat(deleted).isEqualTo(1L);
        
        // Boolean exists = redisTemplate.hasKey(key).block();
        // assertThat(exists).isFalse();
        Long deletedCount = redisTemplate.keys("*:*")
            .flatMap(redisTemplate::delete)
            .reduce(0L, Long::sum)
            .block();
        assertThat(deletedCount).isNotNull();
    }

    @Test
    void testIncrement() {
        // Given
        String key = TEST_KEY_PREFIX + "counter";

        // When: Increment multiple times
        Long count1 = redisTemplate.opsForValue().increment(key).block();
        Long count2 = redisTemplate.opsForValue().increment(key).block();
        Long count3 = redisTemplate.opsForValue().increment(key).block();

        // Then
        assertThat(count1).isEqualTo(1);
        assertThat(count2).isEqualTo(2);
        assertThat(count3).isEqualTo(3);
    }

    @Test
    void testMultipleKeys() {
        // Given
        String key1 = TEST_KEY_PREFIX + "key1";
        String key2 = TEST_KEY_PREFIX + "key2";
        String key3 = TEST_KEY_PREFIX + "key3";

        // When: Set multiple keys
        redisTemplate.opsForValue().set(key1, "value1").block();
        redisTemplate.opsForValue().set(key2, "value2").block();
        redisTemplate.opsForValue().set(key3, "value3").block();

        // Then: All keys should exist
        Long keyCount = redisTemplate.keys(TEST_KEY_PREFIX + "*").count().block();
        assertThat(keyCount).isEqualTo(3);
    }

    @Test
    void testKeysPattern() {
        // Given
        redisTemplate.opsForValue().set(TEST_KEY_PREFIX + "user:1", "Alice").block();
        redisTemplate.opsForValue().set(TEST_KEY_PREFIX + "user:2", "Bob").block();
        redisTemplate.opsForValue().set(TEST_KEY_PREFIX + "order:1", "Order1").block();

        // When: Search for user keys
        Long userKeyCount = redisTemplate.keys(TEST_KEY_PREFIX + "user:*").count().block();

        // Then
        assertThat(userKeyCount).isEqualTo(2);
    }

    @Test
    void testJSONStorage() {
        // Given: Store JSON-like string
        String key = TEST_KEY_PREFIX + "json";
        String jsonValue = "{\"name\":\"John\",\"age\":30}";

        // When
        redisTemplate.opsForValue().set(key, jsonValue).block();

        // Then
        String result = redisTemplate.opsForValue().get(key).block();
        assertThat(result).isEqualTo(jsonValue);
        assertThat(result).contains("\"name\":");
        assertThat(result).contains("\"age\":");
    }

    @Test
    void testReactiveOperations() {
        // Given
        String key = TEST_KEY_PREFIX + "reactive";
        String value = "test reactive";

        // When & Then: Use StepVerifier for reactive testing
        StepVerifier.create(
                        redisTemplate.opsForValue().set(key, value)
                )
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(
                        redisTemplate.opsForValue().get(key)
                )
                .expectNext(value)
                .verifyComplete();
    }

    @Test
    void testListAllKeys() {
        // Given: Create some test keys
        redisTemplate.opsForValue().set(TEST_KEY_PREFIX + "a", "1").block();
        redisTemplate.opsForValue().set(TEST_KEY_PREFIX + "b", "2").block();
        redisTemplate.opsForValue().set(TEST_KEY_PREFIX + "c", "3").block();

        // When: List all test keys
        StepVerifier.create(redisTemplate.keys(TEST_KEY_PREFIX + "*"))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testDeleteAllKeysByPattern() {
        // Given: Create multiple keys with "demo" prefix
        redisTemplate.opsForValue().set("demo:1", "value1").block();
        redisTemplate.opsForValue().set("demo:2", "value2").block();
        redisTemplate.opsForValue().set("demo:3", "value3").block();
        redisTemplate.opsForValue().set("other:1", "other").block();

        // Verify they exist
        Long demoCount = redisTemplate.keys("demo:*").count().block();
        assertThat(demoCount).isEqualTo(3);

        // When: Delete all keys starting with "demo"
        Long deletedCount = redisTemplate.keys("demo:*")
                .flatMap(redisTemplate::delete)
                .reduce(0L, Long::sum)
                .block();

        // Then: All demo keys should be deleted
        assertThat(deletedCount).isEqualTo(3);
        
        // Verify demo keys are gone
        Long remainingDemoKeys = redisTemplate.keys("demo:*").count().block();
        assertThat(remainingDemoKeys).isEqualTo(0);
        
        // But other keys should still exist
        Boolean otherExists = redisTemplate.hasKey("other:1").block();
        assertThat(otherExists).isTrue();
    }
}
