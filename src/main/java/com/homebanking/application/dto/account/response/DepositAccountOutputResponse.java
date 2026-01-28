package com.homebanking.application.dto.account.response;

import java.math.BigDecimal;

public record DepositAccountOutputResponse(
        Long accountId,
        BigDecimal balance
) {}
