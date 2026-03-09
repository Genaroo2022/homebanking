package com.homebanking.adapter.in.web.response;

import java.util.UUID;

public record CardResponse(
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

