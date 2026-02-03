package com.homebanking.adapter.out.security;

import com.homebanking.application.dto.security.RateLimitStatus;
import com.homebanking.port.out.security.LoginRateLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class LoginRateLimiterAdapter implements LoginRateLimiter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final boolean enabled;
    private final long capacity;
    private final long windowSeconds;

    public LoginRateLimiterAdapter(
            @Value("${security.login-rate-limit.enabled:true}") boolean enabled,
            @Value("${security.login-rate-limit.capacity:5}") long capacity,
            @Value("${security.login-rate-limit.window-seconds:900}") long windowSeconds) {
        this.enabled = enabled;
        this.capacity = capacity;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public RateLimitStatus checkLimit(String ipAddress) {
        if (!enabled) {
            return new RateLimitStatus(true, 0);
        }

        Bucket bucket = buckets.computeIfAbsent(ipAddress, key -> createBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return new RateLimitStatus(true, 0);
        }

        long nanosToWait = probe.getNanosToWaitForRefill();
        long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(nanosToWait));
        return new RateLimitStatus(false, retryAfterSeconds);
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.intervally(capacity, Duration.ofSeconds(windowSeconds))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}


