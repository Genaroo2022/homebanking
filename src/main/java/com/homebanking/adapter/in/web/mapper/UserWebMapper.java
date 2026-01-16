package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.RegisterUserRequest;
import com.homebanking.application.dto.registration.request.RegisterUserInputRequest;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public RegisterUserInputRequest toInputRequest(RegisterUserRequest request) {
        return new RegisterUserInputRequest(
                request.getName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getDni(),
                request.getBirthDate(),
                request.getAddress()
        );
    }
}