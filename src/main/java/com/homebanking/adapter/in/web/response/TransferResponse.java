
package com.homebanking.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response: TransferResponse

 * Respuesta exitosa de creación de transferencia.
 * Información que el cliente necesita conocer.

 * Ejemplo JSON:
 * {
 *   "id": "a4b6b0c2-9b1a-4b0e-8b0a-4b0c2d9b1a4b",
 *   "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
 *   "originAccountId": "a4b6b0c2-9b1a-4b0e-8b0a-4b0c2d9b1a4b",
 *   "targetCbu": "1234567890123456789012",
 *   "amount": 100.50,
 *   "description": "Pago de servicios",
 *   "status": "PENDING",
 *   "createdAt": "2024-01-21T15:30:00"
 * }
 */
public record TransferResponse(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("idempotencyKey")
        String idempotencyKey,

        @JsonProperty("originAccountId")
        UUID originAccountId,

        @JsonProperty("targetCbu")
        String targetCbu,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("description")
        String description,

        @JsonProperty("status")
        String status,

        @JsonProperty("createdAt")
        String createdAt
) {}