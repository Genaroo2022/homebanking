package com.homebanking.application.dto.authentication.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutInputRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken,
        String accessToken
) {}
