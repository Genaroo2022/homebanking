package com.homebanking.adapter.out.persistence.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homebanking.domain.model.LoginAttempt;
import com.homebanking.port.out.security.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisLoginAttemptRepositoryAdapter implements LoginAttemptRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String KEY_PREFIX = "login_attempts:";
    private static final int MAX_ATTEMPTS_TO_STORE = 10;
    private static final long TTL_IN_MINUTES = 60;

    public RedisLoginAttemptRepositoryAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        // Register module to handle Java 8 date/time types
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(LoginAttempt attempt) {
        if (attempt.successful()) {
            resetFailedAttempts(attempt.username());
            return;
        }

        String key = KEY_PREFIX + attempt.username();
        redisTemplate.opsForList().leftPush(key, attempt);
        redisTemplate.opsForList().trim(key, 0, MAX_ATTEMPTS_TO_STORE -1);
        redisTemplate.expire(key, TTL_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public List<LoginAttempt> findRecentFailedAttempts(String username) {
        String key = KEY_PREFIX + username;
        List<Object> attempts = redisTemplate.opsForList().range(key, 0, -1);
        if (attempts == null) {
            return List.of();
        }
        return attempts.stream()
                .map(obj -> objectMapper.convertValue(obj, LoginAttempt.class))
                .collect(Collectors.toList());
    }

    @Override
    public void resetFailedAttempts(String username) {
        String key = KEY_PREFIX + username;
        redisTemplate.delete(key);
    }
}


