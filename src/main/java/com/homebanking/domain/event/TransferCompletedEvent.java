package com.homebanking.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCompletedEvent(
        Long transferId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        LocalDateTime completedAt
) {}
