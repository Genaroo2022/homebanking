package com.homebanking.application.dto.profile.request;

/**
 * DTO de entrada para el caso de uso GetUserProfileUseCase.
 * Email del usuario autenticado extraído del JWT.
 */
public record GetUserProfileInputRequest(String email) {}