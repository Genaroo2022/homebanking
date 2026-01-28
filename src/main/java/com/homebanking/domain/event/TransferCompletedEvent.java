package com.homebanking.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCompletedEvent(
        Long transferId,
        Long sourceAccountId,
        String targetCbu,
        BigDecimal amount,
        LocalDateTime completedAt
) {}
