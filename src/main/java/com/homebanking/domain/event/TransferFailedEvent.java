package com.homebanking.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

public record TransferFailedEvent(
        UUID transferId,
        UUID sourceAccountId,
        String targetCbu,
        BigDecimal amount,
        String failureReason,
        LocalDateTime failedAt
) {}


