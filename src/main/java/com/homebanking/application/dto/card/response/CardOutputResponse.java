package com.homebanking.application.dto.card.response;

import java.util.UUID;

public record CardOutputResponse(
        UUID id,
        UUID accountId,
        String maskedNumber,
        String cardHolder,
        String fromDate,
        String thruDate,
        String type,
        String color,
        boolean active
) {
}

