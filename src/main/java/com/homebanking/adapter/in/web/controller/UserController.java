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

@RestController // Indica a Spring que esta clase maneja peticiones HTTP
@RequestMapping("/users") // Ruta base: todas las peticiones aquí empiezan con /users
@RequiredArgsConstructor // Inyección de dependencias automática
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserWebMapper userWebMapper;

    @PostMapping // Maneja peticiones POST a /users
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {

        // 1. Convertimos el JSON (Request) a Dominio (User)
        User user = userWebMapper.toDomain(request);

        // 2. Ejecutamos la lógica de negocio
        registerUserUseCase.register(user);

        // 3. Devolvemos respuesta 201 Created (Éxito)
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}