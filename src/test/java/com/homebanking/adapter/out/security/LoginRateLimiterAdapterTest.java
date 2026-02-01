package com.homebanking.adapter.out.security;

import com.homebanking.application.dto.security.RateLimitStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterAdapterTest {

    @Test
    void shouldAllowWhenDisabled() {
        LoginRateLimiterAdapter limiter = new LoginRateLimiterAdapter(false, 1, 1);

        RateLimitStatus result = limiter.checkLimit("10.0.0.1");

        assertThat(result.allowed()).isTrue();
        assertThat(result.retryAfterSeconds()).isZero();
    }

    @Test
    void shouldBlockAfterCapacityExceeded() {
        LoginRateLimiterAdapter limiter = new LoginRateLimiterAdapter(true, 2, 60);
        String ip = "10.0.0.2";

        RateLimitStatus first = limiter.checkLimit(ip);
        RateLimitStatus second = limiter.checkLimit(ip);
        RateLimitStatus third = limiter.checkLimit(ip);

        assertThat(first.allowed()).isTrue();
        assertThat(second.allowed()).isTrue();
        assertThat(third.allowed()).isFalse();
        assertThat(third.retryAfterSeconds()).isGreaterThanOrEqualTo(1);
    }
}


