package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.UserWebMapper;
import com.homebanking.adapter.in.web.request.RegisterUserRequest;
import com.homebanking.application.dto.registration.request.RegisterUserInputRequest;
import com.homebanking.port.in.registration.RegisterUserInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserInputPort registerUserUseCase;
    private final UserWebMapper userWebMapper;

    @PostMapping
    public ResponseEntity<Map<String, Long>> register(
            @Valid @RequestBody RegisterUserRequest request) {

        RegisterUserInputRequest inputRequest = userWebMapper.toInputRequest(request);

        var outputResponse = registerUserUseCase.register(inputRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", outputResponse.userId()));
    }
}