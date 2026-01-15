package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.UserWebMapper;
import com.homebanking.adapter.in.web.request.LoginRequest;
import com.homebanking.adapter.in.web.response.TokenResponse;
import com.homebanking.adapter.in.web.security.JwtService;
import com.homebanking.application.usecase.LoginUserUseCase;
import com.homebanking.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.homebanking.application.usecase.GetUserProfileUseCase;
import com.homebanking.adapter.in.web.response.MeResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUserUseCase loginUserUseCase;
    private final JwtService jwtService;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UserWebMapper userWebMapper;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {

        User authenticatedUser = loginUserUseCase.login(request.getEmail(), request.getPassword());

        String token = jwtService.generateToken(authenticatedUser.getEmail());

        return ResponseEntity.ok(new TokenResponse(token));

    }
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {

        var result = getUserProfileUseCase.getUserProfile(userDetails.getUsername());
        MeResponse response = userWebMapper.toMeResponse(result);
        return ResponseEntity.ok(response);
    }
}