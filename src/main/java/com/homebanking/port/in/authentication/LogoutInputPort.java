package com.homebanking.port.in.authentication;

import com.homebanking.application.dto.authentication.request.LogoutInputRequest;

public interface LogoutInputPort {
    void logout(LogoutInputRequest request);
}
