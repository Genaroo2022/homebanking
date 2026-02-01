package com.homebanking.application.dto.transfer.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO: TransferOutputResponse

 * Salida del caso de uso para crear transferencia.
 * Información completa de la transferencia creada.
 */
public record TransferOutputResponse(
        UUID id,
        String idempotencyKey,
        UUID originAccountId,
        String targetCbu,
        BigDecimal amount,
        String description,
        String status,
        String createdAt
) {}

