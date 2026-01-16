package com.homebanking.port.in.authentication;

import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.domain.exception.InvalidUserDataException;

public interface LoginUserInputPort {TokenOutputResponse login(LoginInputRequest request);}
