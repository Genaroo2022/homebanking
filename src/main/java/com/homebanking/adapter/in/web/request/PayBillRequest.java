package com.homebanking.adapter.in.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PayBillRequest(
        @NotNull UUID accountId,
        @NotBlank String billerCode,
        @NotBlank String reference,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}

