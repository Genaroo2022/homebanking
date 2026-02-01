package com.homebanking.application.dto.profile.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida del caso de uso GetUserProfileUseCase.
 * Contiene toda la información del perfil del usuario incluyendo cuentas.
 */
public record UserProfileOutputResponse(
        UUID id,
        String email,
        String name,
        String lastName,
        List<AccountOutputResponse> accounts
) {
    public record AccountOutputResponse(
            UUID id,
            String cbu,
            String alias,
            BigDecimal balance
    ) {}
}

