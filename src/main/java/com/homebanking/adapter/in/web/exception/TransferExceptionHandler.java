package com.homebanking.adapter.in.web.exception;

import com.homebanking.adapter.in.web.response.ErrorResponse;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.account.InsufficientFundsException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.SameAccountTransferException;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.exception.transfer.TransferProcessingException;
import com.homebanking.domain.exception.transfer.DestinationAccountNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception Handler: TransferExceptionHandler

 * Centraliza el manejo de excepciones de transferencias.
 * Mapea excepciones de dominio a respuestas HTTP semánticamente correctas.

 * Principios:
 * • Cada excepción de dominio → código HTTP específico
 * • Logging diferenciado (warn para negocio, error para técnico)
 * • Mensajes claros para el usuario final
 * • Rastreabilidad para debugging
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TransferExceptionHandler {

    /**
     * Manejar: InsufficientFundsException

     * Status: 409 Conflict
     * Razón: El recurso (saldo) entra en conflicto con la solicitud.

     * No es un error del cliente (4xx genérico), es un conflicto de estado.
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            InsufficientFundsException ex) {

        log.warn("Saldo insuficiente para transferencia: account={}, requested={}, available={}",
                ex.getAccountId(), ex.getRequestedAmount(), ex.getAvailableBalance());

        ErrorResponse error = ErrorResponse.of(
                "INSUFFICIENT_FUNDS",
                "No tiene saldo suficiente para realizar esta transferencia. " +
                        "Solicitado: " + ex.getRequestedAmount() +
                        ", Disponible: " + ex.getAvailableBalance()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Manejar: AccountNotFoundException

     * Status: 404 Not Found
     * Razón: La cuenta no existe en el sistema.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex) {

        log.warn("Cuenta no encontrada: {}", ex.getAccountId());

        ErrorResponse error = ErrorResponse.of(
                "ACCOUNT_NOT_FOUND",
                "La cuenta especificada no existe en el sistema"
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /**
     * Manejar: TransferNotFoundException
     *
     * Status: 404 Not Found
     */
    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransferNotFound(
            TransferNotFoundException ex) {

        log.warn("Transferencia no encontrada: {}", ex.getTransferId());

        ErrorResponse error = ErrorResponse.of(
                "TRANSFER_NOT_FOUND",
                "La transferencia especificada no existe"
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /**
     * Manejar: DestinationAccountNotFoundException
     *
     * Status: 404 Not Found
     */
    @ExceptionHandler(DestinationAccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDestinationAccountNotFound(
            DestinationAccountNotFoundException ex) {

        log.warn("Cuenta destino no encontrada: {}", ex.getTargetCbu());

        ErrorResponse error = ErrorResponse.of(
                "DESTINATION_ACCOUNT_NOT_FOUND",
                "La cuenta destino especificada no existe en el sistema"
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /**
     * Manejar: SameAccountTransferException

     * Status: 409 Conflict
     * Razón: La solicitud intenta una operación inválida (transferencia a sí misma).
     */
    @ExceptionHandler(SameAccountTransferException.class)
    public ResponseEntity<ErrorResponse> handleSameAccountTransfer(
            SameAccountTransferException ex) {

        log.warn("Intento de transferencia a la misma cuenta: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "SAME_ACCOUNT_TRANSFER",
                "No puede realizar una transferencia hacia la misma cuenta de origen"
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Manejar: InvalidTransferDataException

     * Status: 400 Bad Request
     * Razón: Datos de entrada inválidos (CBU, monto, etc.).
     */
    @ExceptionHandler(InvalidTransferDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransferData(
            InvalidTransferDataException ex) {

        log.warn("Error de datos de transferencia: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "INVALID_TRANSFER_DATA",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Manejar: TransferProcessingException

     * Status: 502 Bad Gateway (si error externo irrecuperable)
     *         503 Service Unavailable (si error temporal/recuperable)

     * Razón: Error al procesar transferencia en sistema externo.
     */
    @ExceptionHandler(TransferProcessingException.class)
    public ResponseEntity<ErrorResponse> handleTransferProcessing(
            TransferProcessingException ex) {

        HttpStatus status = ex.isRecoverable()
                ? HttpStatus.SERVICE_UNAVAILABLE  // 503: reintente después
                : HttpStatus.BAD_GATEWAY;         // 502: error del servidor remoto

        log.error("Error procesando transferencia: code={}, recoverable={}, message={}",
                ex.getExternalErrorCode(), ex.isRecoverable(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "TRANSFER_PROCESSING_ERROR",
                "No se pudo procesar la transferencia en el sistema externo. " +
                        "Intente más tarde."
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}


