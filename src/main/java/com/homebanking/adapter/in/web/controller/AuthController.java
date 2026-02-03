package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.annotation.Auditable;
import com.homebanking.adapter.in.web.mapper.AuthWebMapper;
import com.homebanking.adapter.in.web.request.EnableTotpRequest;
import com.homebanking.adapter.in.web.request.LoginRequest;
import com.homebanking.adapter.in.web.request.LogoutRequest;
import com.homebanking.adapter.in.web.request.RefreshTokenRequest;
import com.homebanking.adapter.in.web.response.TokenResponse;
import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.request.LogoutInputRequest;
import com.homebanking.application.dto.authentication.request.RefreshTokenInputRequest;
import com.homebanking.application.dto.profile.request.GetUserProfileInputRequest;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.in.authentication.LogoutInputPort;
import com.homebanking.port.in.authentication.RefreshTokenInputPort;
import com.homebanking.port.in.authentication.StartTotpSetupInputPort;
import com.homebanking.port.in.authentication.EnableTotpInputPort;
import com.homebanking.port.in.authentication.GetTotpProvisioningUriInputPort;
import com.homebanking.port.in.profile.GetUserProfileInputPort;
import com.homebanking.adapter.in.web.response.MeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final LoginUserInputPort loginUserUseCase;
    private final GetUserProfileInputPort getUserProfileUseCase;
    private final RefreshTokenInputPort refreshTokenUseCase;
    private final LogoutInputPort logoutUseCase;
    private final StartTotpSetupInputPort startTotpSetupUseCase;
    private final EnableTotpInputPort enableTotpUseCase;
    private final GetTotpProvisioningUriInputPort getTotpProvisioningUriUseCase;

     private final AuthWebMapper authWebMapper;


    @PostMapping("/login")
    @Auditable(action = "auth.login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid LoginRequest request, HttpServletRequest httpServletRequest) {

        LoginInputRequest inputRequest = authWebMapper.toInputRequest(request, httpServletRequest.getRemoteAddr());

        var outputResponse = loginUserUseCase.login(inputRequest);

        TokenResponse response = authWebMapper.toResponse(outputResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Auditable(action = "auth.refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody @Valid RefreshTokenRequest request) {

        RefreshTokenInputRequest inputRequest =
                new RefreshTokenInputRequest(request.getRefreshToken());

        var outputResponse = refreshTokenUseCase.refresh(inputRequest);

        TokenResponse response = authWebMapper.toResponse(outputResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Auditable(action = "auth.logout")
    public ResponseEntity<Void> logout(
            @RequestBody @Valid LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        LogoutInputRequest inputRequest =
                new LogoutInputRequest(request.getRefreshToken(), accessToken);

        logoutUseCase.logout(inputRequest);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/2fa/setup")
    @Auditable(action = "auth.2fa.setup")
    public ResponseEntity<Void> startTotpSetup(
            @AuthenticationPrincipal UserDetails userDetails) {
        startTotpSetupUseCase.startSetup(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/2fa/qr")
    @Auditable(action = "auth.2fa.qr")
    public ResponseEntity<byte[]> getTotpQr(
            @AuthenticationPrincipal UserDetails userDetails) {
        String uri = getTotpProvisioningUriUseCase.getProvisioningUri(userDetails.getUsername());
        byte[] png = renderQrPng(uri, 240, 240);
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(png);
    }

    @PostMapping("/2fa/enable")
    @Auditable(action = "auth.2fa.enable")
    public ResponseEntity<Void> enableTotp(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid EnableTotpRequest request) {
        enableTotpUseCase.enable(userDetails.getUsername(), request.getCode());
        return ResponseEntity.noContent().build();
    }

    private byte[] renderQrPng(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, Map.of());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar QR", ex);
        }
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

