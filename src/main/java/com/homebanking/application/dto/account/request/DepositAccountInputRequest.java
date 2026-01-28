package com.homebanking.application.dto.account.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositAccountInputRequest(
        @NotNull(message = "El ID de la cuenta es obligatorio")
        Long accountId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0.01")
        BigDecimal amount
) {}
