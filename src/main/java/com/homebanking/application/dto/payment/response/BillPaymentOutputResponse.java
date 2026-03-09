package com.homebanking.application.dto.payment.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BillPaymentOutputResponse(
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

