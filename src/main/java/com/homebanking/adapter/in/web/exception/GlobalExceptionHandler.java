package com.homebanking.adapter.in.web.exception;

import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.domain.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // 1. Esto le dice a Spring: "Escucha a TODOS los controladores"
public class GlobalExceptionHandler {

    // 2. Atrapamos el error específico de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // 3. Recorremos todos los errores que encontró Spring (pueden ser varios)
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 4. Devolvemos un 400 Bad Request pero con NUESTRO mapa limpio
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 1. Para errores de lógica de negocio (ej: menor de edad) -> 400 Bad Request
    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<Map<String, String>> handleInvalidUserData(InvalidUserDataException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 2. Para conflictos de duplicados (ej: email repetido) -> 409 Conflict
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage()); // Aquí llegará el mensaje de "DomainErrorMessages"
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}