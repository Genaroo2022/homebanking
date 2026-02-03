package com.homebanking.application.dto.authentication.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para el caso de uso LoginUserUseCase.
 * Record immutable para garantizar thread-safety.
 */
public record LoginInputRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        String ipAddress,
        String totpCode
) {}

