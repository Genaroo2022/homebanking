package com.homebanking.application.dto.authentication.response;

/**
 * DTO de salida del caso de uso LoginUserUseCase.
 * Contiene el JWT generado tras autenticación exitosa.
 */
public record TokenOutputResponse(String token) {}
