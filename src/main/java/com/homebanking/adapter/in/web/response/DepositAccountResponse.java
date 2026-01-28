package com.homebanking.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record DepositAccountResponse(
        @JsonProperty("accountId")
        Long accountId,

        @JsonProperty("balance")
        BigDecimal balance
) {}
