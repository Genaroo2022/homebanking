package com.homebanking.application.dto.security;

public record RateLimitStatus(
        boolean allowed,
        long retryAfterSeconds
) {
}
