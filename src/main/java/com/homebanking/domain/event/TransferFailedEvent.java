package com.homebanking.domain.event;

import java.time.LocalDateTime;

import java.math.BigDecimal;

public record TransferFailedEvent(
        Long transferId,
        Long sourceAccountId,
        String targetCbu,
        BigDecimal amount,
        String failureReason,
        LocalDateTime failedAt
) {}
