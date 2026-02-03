package com.homebanking.port.in.authentication;

import com.homebanking.application.dto.authentication.request.RefreshTokenInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;

public interface RefreshTokenInputPort {
    TokenOutputResponse refresh(RefreshTokenInputRequest request);
}
