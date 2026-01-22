package com.homebanking.application.dto.transfer.response;

import java.math.BigDecimal;

/**
 * DTO: TransferOutputResponse

 * Salida del caso de uso para crear transferencia.
 * Información completa de la transferencia creada.
 */
public record TransferOutputResponse(
        Long id,
        String idempotencyKey,
        Long originAccountId,
        String targetCbu,
        BigDecimal amount,
        String description,
        String status,
        String createdAt
) {}