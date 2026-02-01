package com.homebanking.adapter.in.web.exception;

import com.homebanking.adapter.in.web.response.ErrorResponse;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.exception.card.InvalidCardDataException;
import com.homebanking.domain.exception.transfer.DestinationAccountNotFoundException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.exception.user.TooManyLoginAttemptsException;
import com.homebanking.domain.exception.user.UserAlreadyExistsException;
import com.homebanking.application.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    log.warn("Validacion fallida en campo: {} - {}",
                            error.getField(), error.getDefaultMessage());
                    return error.getField() + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_ERROR",
                message.isBlank() ? "Validacion fallida" : message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserData(
            InvalidUserDataException ex) {

        log.warn("Error de datos de usuario: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "INVALID_USER_DATA",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyLoginAttempts(
            TooManyLoginAttemptsException ex) {

        log.warn("Demasiados intentos de login. Retry-After: {}s", ex.getRetryAfterSeconds());

        ErrorResponse error = ErrorResponse.of(
                "TOO_MANY_LOGIN_ATTEMPTS",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(error);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex) {

        log.warn("Rate limit excedido. Retry-After: {}s", ex.getRetryAfterSeconds());

        ErrorResponse error = ErrorResponse.of(
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex) {

        log.warn("Intento de crear usuario duplicado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "USER_ALREADY_EXISTS",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(InvalidAccountDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAccountData(
            InvalidAccountDataException ex) {

        log.warn("Error de datos de cuenta: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "INVALID_ACCOUNT_DATA",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(InvalidCardDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCardData(
            InvalidCardDataException ex) {

        log.warn("Error de datos de tarjeta: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "INVALID_CARD_DATA",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

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

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex) {

        log.warn("Cuenta no encontrada: {}", ex.getAccountId());

        ErrorResponse error = ErrorResponse.of(
                "ACCOUNT_NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(DestinationAccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDestinationAccountNotFound(
            DestinationAccountNotFoundException ex) {

        log.warn("Cuenta destino no encontrada: {}", ex.getTargetCbu());

        ErrorResponse error = ErrorResponse.of(
                "DESTINATION_ACCOUNT_NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransferNotFound(
            TransferNotFoundException ex) {

        log.warn("Transferencia no encontrada: {}", ex.getTransferId());

        ErrorResponse error = ErrorResponse.of(
                "TRANSFER_NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex) {

        ErrorResponse error = ErrorResponse.of(
                "RESOURCE_NOT_FOUND",
                "Ruta no encontrada: " + ex.getResourcePath()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("Error no controlado", ex);

        ErrorResponse error = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "Ha ocurrido un error inesperado. Por favor, intente mas tarde."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
