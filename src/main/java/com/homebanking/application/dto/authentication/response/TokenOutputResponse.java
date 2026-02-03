package com.homebanking.application.dto.authentication.response;

/**
 * DTO de salida para autenticación y refresh.
 */
public record TokenOutputResponse(String accessToken, String refreshToken) {}
