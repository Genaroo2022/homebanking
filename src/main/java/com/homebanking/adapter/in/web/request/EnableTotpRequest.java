package com.homebanking.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnableTotpRequest {
    @NotBlank(message = "El código TOTP es obligatorio")
    private String code;
}
