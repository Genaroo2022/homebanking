package com.homebanking.application.dto.registration.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO de entrada para el caso de uso RegisterUserUseCase.
 * Contiene todos los datos necesarios para crear un nuevo usuario.
 */
public record RegisterUserInputRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        @NotBlank(message = "El DNI es obligatorio")
        String dni,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        LocalDate birthDate,

        @NotBlank(message = "La dirección es obligatoria")
        String address
) {}

