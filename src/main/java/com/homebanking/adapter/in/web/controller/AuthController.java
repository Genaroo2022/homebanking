package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.AuthWebMapper;
import com.homebanking.adapter.in.web.request.LoginRequest;
import com.homebanking.adapter.in.web.response.TokenResponse;
import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.profile.request.GetUserProfileInputRequest;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.in.profile.GetUserProfileInputPort;
import com.homebanking.adapter.in.web.response.MeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final LoginUserInputPort loginUserUseCase;
    private final GetUserProfileInputPort getUserProfileUseCase;

     private final AuthWebMapper authWebMapper;


    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid LoginRequest request) {

        LoginInputRequest inputRequest = authWebMapper.toInputRequest(request);

        var outputResponse = loginUserUseCase.login(inputRequest);

        TokenResponse response = authWebMapper.toResponse(outputResponse);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<MeResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        GetUserProfileInputRequest inputRequest =
                new GetUserProfileInputRequest(userDetails.getUsername());

        var outputResponse = getUserProfileUseCase.getUserProfile(inputRequest);

        MeResponse response = authWebMapper.toMeResponse(outputResponse);

        return ResponseEntity.ok(response);
    }
}