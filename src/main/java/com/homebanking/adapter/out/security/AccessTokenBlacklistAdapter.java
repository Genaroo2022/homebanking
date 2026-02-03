package com.homebanking.adapter.out.security;

import com.homebanking.port.out.auth.AccessTokenStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Component
public class AccessTokenBlacklistAdapter implements AccessTokenStore {

    private static final String KEY_PREFIX = "access-blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

    public AccessTokenBlacklistAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        String key = toKey(accessToken);
        Object value = redisTemplate.opsForValue().get(key);
        return Boolean.TRUE.equals(value);
    }

    @Override
    public void blacklist(String accessToken, long expiresAtMillis) {
        long ttlMillis = expiresAtMillis - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            return;
        }
        String key = toKey(accessToken);
        redisTemplate.opsForValue().set(key, true, Duration.ofMillis(ttlMillis));
    }

    private String toKey(String accessToken) {
        return KEY_PREFIX + sha256Hex(accessToken);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
