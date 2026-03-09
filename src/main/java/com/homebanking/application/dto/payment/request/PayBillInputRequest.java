package com.homebanking.application.dto.payment.request;

import java.math.BigDecimal;
import java.util.UUID;

public record PayBillInputRequest(
        UUID accountId,
        String billerCode,
        String reference,
        BigDecimal amount,
        String idempotencyKey,
        String requesterEmail
) {
}

