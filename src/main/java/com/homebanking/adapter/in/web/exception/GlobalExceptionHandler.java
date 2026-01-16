package com.homebanking.adapter.in.web.exception;

import com.homebanking.adapter.in.web.response.ErrorResponse;
import com.homebanking.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);

            log.warn("Validación fallida en campo: {} - {}", fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
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

    /**
     * Maneja excepciones de datos de transferencia inválidos.

     * Response: 400 Bad Request
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
     * Captura cualquier excepción no controlada.
     * Fallback para excepciones inesperadas.

     * Response: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("Error no controlado", ex);

        ErrorResponse error = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "Ha ocurrido un error inesperado. Por favor, intente más tarde."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}