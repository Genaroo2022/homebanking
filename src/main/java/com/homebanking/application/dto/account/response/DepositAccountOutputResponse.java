package com.homebanking.application.dto.account.response;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositAccountOutputResponse(
        UUID accountId,
        BigDecimal balance
) {}


