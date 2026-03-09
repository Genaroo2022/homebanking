package com.homebanking.adapter.in.web.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BillPaymentResponse(
        UUID id,
        UUID accountId,
        String billerCode,
        String reference,
        BigDecimal amount,
        String status,
        String failureReason,
        String createdAt,
        String processedAt
) {
}

