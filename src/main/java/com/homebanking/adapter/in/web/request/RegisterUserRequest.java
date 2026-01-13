package com.homebanking.adapter.in.web.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class RegisterUserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate birthDate;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;
}