package com.homebanking.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
