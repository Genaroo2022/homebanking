package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.UserWebMapper;
import com.homebanking.adapter.in.web.request.RegisterUserRequest;
import com.homebanking.application.usecase.RegisterUserUseCase;
import com.homebanking.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserWebMapper userWebMapper;

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {

        User user = userWebMapper.toDomain(request);

        registerUserUseCase.register(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}