package com.homebanking.port.out.security;

import com.homebanking.application.dto.security.RateLimitStatus;

public interface LoginRateLimiter {
    RateLimitStatus checkLimit(String ipAddress);
}



