package com.homebanking.adapter.in.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request: CreateTransferRequest

 * Mapea el body JSON de la solicitud HTTP.
 * Validaciones:
 * - Campos requeridos
 * - Formato de CBU (se valida en dominio)
 * - Monto positivo
 * - idempotencyKey via header "Idempotency-Key"

 * Ejemplo JSON:
 * {
 *   "originAccountId": "a4b6b0c2-9b1a-4b0e-8b0a-4b0c2d9b1a4b",
 *   "targetCbu": "1234567890123456789012",
 *   "amount": 100.50,
 *   "description": "Pago de servicios"
 * }
 */
public record CreateTransferRequest(
        @NotNull UUID originAccountId,
        @NotBlank String targetCbu,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String description
) {}


