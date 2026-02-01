package com.homebanking.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID transferId,
        UUID sourceAccountId,
        String targetCbu,
        BigDecimal amount,
        LocalDateTime completedAt
) {}
