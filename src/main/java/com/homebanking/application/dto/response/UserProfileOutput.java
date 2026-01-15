package com.homebanking.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record UserProfileOutput(
        Long id,
        String email,
        String name,
        String lastName,
        List<AccountOutput> accounts
) {
    public record AccountOutput(
            Long id,
            String cbu,
            String alias,
            BigDecimal balance
    ) {}
}