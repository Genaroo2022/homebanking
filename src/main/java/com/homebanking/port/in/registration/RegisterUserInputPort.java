package com.homebanking.port.in.registration;

import com.homebanking.application.dto.registration.request.RegisterUserInputRequest;
import com.homebanking.application.dto.registration.response.UserRegisteredOutputResponse;

public interface RegisterUserInputPort {UserRegisteredOutputResponse register(RegisterUserInputRequest request);}