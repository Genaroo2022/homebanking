package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.TransferWebMapper;
import com.homebanking.adapter.in.web.request.CreateTransferRequest;
import com.homebanking.adapter.in.web.response.TransferResponse;
import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.domain.exception.*;
import com.homebanking.port.in.transfer.CreateTransferInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller: TransferController

 * Responsabilidades:
 * ✓ Mapear HTTP → DTO "s" de entrada
 * ✓ Invocar use cases
 * ✓ Mapear DTO "s" salida → HTTP response
 * ✓ Manejar excepciones de dominio
 * ✓ Retornar códigos HTTP apropiados

 * No responsable de:
 * ✗ Lógica de negocio (pertenece al "use case")
 * ✗ Persistencia (adapter)
 * ✗ Validaciones complejas (dominio)

 * HTTP Semantics:
 * • 201 Created: Transferencia creada (con Location header)
 * • 400 Bad Request: Datos inválidos
 * • 409 Conflict: Fondos insuficientes, misma cuenta
 * • 404 Not Found: Cuenta no existe
 * • 500 Internal Server Error: Error inesperado

 * Idempotencia:
 * El cliente genera un UUID para cada transferencia intencional (idempotencyKey).
 * Si el servidor recibe la misma key dos veces, devuelve 200 + datos de la primera.
 */
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final CreateTransferInputPort createTransferUseCase;
    private final TransferWebMapper transferWebMapper;

    /**
     * POST /transfers

     * Crea una nueva transferencia.

     * IMPORTANTE: El cliente DEBE enviar un idempotencyKey único.
     * Esto garantiza idempotencia: múltiples POST con el mismo key
     * devuelven el mismo resultado sin duplicar la operación.
     *
     * @param request Datos de la transferencia (con idempotencyKey)
     * @param userDetails Usuario autenticado (del JWT)
     * @return 201 Created con datos de la transferencia creada

     * Códigos de respuesta:
     * • 201: Transferencia creada
     * • 200: Transferencia ya existía con ese idempotencyKey (idempotencia)
     * • 400: Datos inválidos (CBU, monto, etc.)
     * • 404: Cuenta origen no existe
     * • 409: Fondos insuficientes o intento de transferencia a sí mismo
     * • 500: Error interno del servidor
     */
    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Solicitud de transferencia recibida: user={}, account={}, target={}, amount={}",
                userDetails.getUsername(),
                request.getOriginAccountId(), //ANTES SIN GET, CHEQUEAR TAMBIEN, ANTES ESTABA: request.originAccountId()
                request.getTargetCbu(), // LO MISMO QUE ARRIBA
                request.getAmount());//LO MISMO

        try {
            // Mapear request HTTP a DTO del use case
            CreateTransferInputRequest inputRequest = transferWebMapper.toInputRequest(request);

            // Invocar use case (donde ocurre la lógica)
            var outputResponse = createTransferUseCase.createTransfer(inputRequest);

            // Mapear respuesta a formato HTTP
            TransferResponse response = transferWebMapper.toResponse(outputResponse);

            log.info("Transferencia creada exitosamente: id={}, idempotencyKey={}",
                    outputResponse.id(), outputResponse.idempotencyKey());

            // 201 Created es el código correcto por HTTP "semántica"
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (AccountNotFoundException ex) {
            log.warn("Cuenta no encontrada: {}", ex.getAccountId());
            throw ex; // Será manejada por GlobalExceptionHandler

        } catch (InsufficientFundsException ex) {
            log.warn("Fondos insuficientes: account={}, requested={}, available={}",
                    ex.getAccountId(), ex.getRequestedAmount(), ex.getAvailableBalance());
            throw ex;

        } catch (SameAccountTransferException ex) {
            log.warn("Intento de transferencia a la misma cuenta");
            throw ex;

        } catch (InvalidTransferDataException ex) {
            log.warn("Datos de transferencia inválidos: {}", ex.getMessage());
            throw ex;

        } catch (Exception ex) {
            log.error("Error inesperado al crear transferencia", ex);
            throw ex;
        }
    }
}