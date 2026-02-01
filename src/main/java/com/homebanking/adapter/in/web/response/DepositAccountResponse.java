package com.homebanking.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record DepositAccountResponse(
        @JsonProperty("accountId")
        UUID accountId,

        @JsonProperty("balance")
        BigDecimal balance
) {}


