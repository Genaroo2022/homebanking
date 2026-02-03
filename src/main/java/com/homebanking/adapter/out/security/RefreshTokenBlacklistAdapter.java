package com.homebanking.adapter.out.security;

import com.homebanking.port.out.auth.RefreshTokenStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Component
public class RefreshTokenBlacklistAdapter implements RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh-blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

    public RefreshTokenBlacklistAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isBlacklisted(String refreshToken) {
        String key = toKey(refreshToken);
        Object value = redisTemplate.opsForValue().get(key);
        return Boolean.TRUE.equals(value);
    }

    @Override
    public void blacklist(String refreshToken, long expiresAtMillis) {
        long ttlMillis = expiresAtMillis - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            return;
        }
        String key = toKey(refreshToken);
        redisTemplate.opsForValue().set(key, true, Duration.ofMillis(ttlMillis));
    }

    private String toKey(String refreshToken) {
        return KEY_PREFIX + sha256Hex(refreshToken);
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
