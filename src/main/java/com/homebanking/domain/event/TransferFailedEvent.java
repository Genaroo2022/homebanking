package com.homebanking.domain.event;

import java.time.LocalDateTime;

public record TransferFailedEvent(
        Long transferId,
        Long sourceAccountId,
        String failureReason,
        LocalDateTime failedAt
) {}
