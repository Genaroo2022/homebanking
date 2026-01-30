
package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.CreateTransferRequest;
import com.homebanking.adapter.in.web.response.TransferResponse;
import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper: TransferWebMapper

 * Convierte entre capas web (HTTP) y aplicación (DTO "s").

 * Flujo:
 * HTTP Request → CreateTransferRequest → CreateTransferInputRequest → Use Case
 * "Use Case" → TransferOutputResponse → TransferResponse → HTTP Response

 * Responsabilidades:
 * ✓ Transformar datos de request a DTO
 * ✓ Transformar respuesta de "use case" a formato HTTP
 * ✓ Aplicar transformaciones específicas de presentación

 * No responsable de:
 * ✗ Validación (hecha en request + dominio)
 * ✗ Lógica de negocio
 * ✗ Persistencia
 */
@Component
public class TransferWebMapper {

    /**
     * Convierte CreateTransferRequest HTTP a CreateTransferInputRequest (DTO del use case).
     */
    public CreateTransferInputRequest toInputRequest(
            CreateTransferRequest request,
            String idempotencyKey) {
        return new CreateTransferInputRequest(
                request.originAccountId(),
                request.targetCbu(),
                request.amount(),
                request.description(),
                idempotencyKey
        );
    }

    /**
     * Convierte TransferOutputResponse (salida del use case) a TransferResponse HTTP.
     */
    public TransferResponse toResponse(TransferOutputResponse output) {
        return new TransferResponse(
                output.id(),
                output.idempotencyKey(),
                output.originAccountId(),
                output.targetCbu(),
                output.amount(),
                output.description(),
                output.status(),
                output.createdAt()
        );
    }
}
