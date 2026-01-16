package com.homebanking.application.dto.registration.response;

/**
 * DTO de salida del caso de uso RegisterUserUseCase.
 * Retorna solo el ID del usuario creado para seguridad.
 */
public record UserRegisteredOutputResponse(Long userId) {}