package com.homebanking.application.dto.transfer.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO: CreateTransferInputRequest

 * Entrada al caso de uso para crear una transferencia.
 * Contiene solo datos de negocio, no técnicos.
 */
public record CreateTransferInputRequest(
        @NotNull(message = "El ID de la cuenta origen es obligatorio")
        Long originAccountId,

        @NotBlank(message = "El CBU de destino es obligatorio")
        String targetCbu,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0.01")
        BigDecimal amount,

        @NotBlank(message = "La descripción es obligatoria")
        String description,

        @NotBlank(message = "La idempotency key es obligatoria")
        String idempotencyKey
) {}