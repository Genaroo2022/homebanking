package com.homebanking.domain.event;

import com.homebanking.domain.valueobject.user.UserEmail;

import java.time.LocalDateTime;

public record LoginAttemptedEvent(
        UserEmail email,
        String ipAddress,
        boolean successful,
        boolean blocked,
        LocalDateTime occurredAt
) {
}


