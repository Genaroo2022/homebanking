package com.homebanking.adapter.in.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Request: CreateTransferRequest

 * Mapea el body JSON de la solicitud HTTP.
 * Validaciones:
 * - Campos requeridos
 * - Formato de CBU (se valida en dominio)
 * - Monto positivo
 * - idempotencyKey único (previene duplicados)

 * Ejemplo JSON:
 * {
 *   "originAccountId": 1,
 *   "targetCbu": "1234567890123456789012",
 *   "amount": 100.50,
 *   "description": "Pago de servicios",
 *   "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000"
 * }
 */
@Data
@NoArgsConstructor
public class CreateTransferRequest {

    @NotNull(message = "El ID de la cuenta origen es obligatorio")
    private Long originAccountId;

    @NotBlank(message = "El CBU de destino es obligatorio")
    private String targetCbu;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0.01")
    private BigDecimal amount;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotBlank(message = "La idempotency key es obligatoria")
    private String idempotencyKey;
}