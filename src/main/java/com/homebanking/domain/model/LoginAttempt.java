package com.homebanking.domain.model;

import java.time.LocalDateTime;

public record LoginAttempt(
        String username,
        String ipAddress,
        LocalDateTime timestamp,
        boolean successful
) {
}
