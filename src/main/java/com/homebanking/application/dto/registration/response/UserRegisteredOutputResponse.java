package com.homebanking.application.dto.registration.response;

import java.util.UUID;

/**
 * DTO de salida del caso de uso RegisterUserUseCase.
 * Retorna solo el ID del usuario creado para seguridad.
 */
public record UserRegisteredOutputResponse(UUID userId) {}