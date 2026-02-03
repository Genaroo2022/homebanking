package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.AuthWebMapper;
import com.homebanking.adapter.in.web.request.LogoutRequest;
import com.homebanking.adapter.in.web.request.RefreshTokenRequest;
import com.homebanking.adapter.in.web.response.TokenResponse;
import com.homebanking.application.dto.authentication.request.LogoutInputRequest;
import com.homebanking.application.dto.authentication.request.RefreshTokenInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.in.authentication.LogoutInputPort;
import com.homebanking.port.in.authentication.RefreshTokenInputPort;
import com.homebanking.port.in.authentication.StartTotpSetupInputPort;
import com.homebanking.port.in.authentication.EnableTotpInputPort;
import com.homebanking.port.in.authentication.GetTotpProvisioningUriInputPort;
import com.homebanking.port.in.profile.GetUserProfileInputPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private RefreshTokenInputPort refreshTokenUseCase;

    @Autowired
    private AuthWebMapper authWebMapper;

    @Autowired
    private LogoutInputPort logoutUseCase;

    @Test
    void shouldRefreshTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh_token_123");

        TokenOutputResponse output = new TokenOutputResponse("access_1", "refresh_2");
        TokenResponse expected = new TokenResponse("access_1", "refresh_2");

        when(refreshTokenUseCase.refresh(new RefreshTokenInputRequest("refresh_token_123")))
                .thenReturn(output);
        when(authWebMapper.toResponse(output)).thenReturn(expected);

        var response = authController.refresh(request);

        assertThat(response.getBody()).isEqualTo(expected);
        verify(refreshTokenUseCase).refresh(new RefreshTokenInputRequest("refresh_token_123"));
        verify(authWebMapper).toResponse(output);
    }

    @Test
    void shouldLogout() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh_token_123");

        authController.logout(request, null);

        verify(logoutUseCase).logout(new LogoutInputRequest("refresh_token_123", null));
    }

    @Configuration
    static class TestConfig {

        @Bean
        AuthController authController(
                LoginUserInputPort loginUserUseCase,
                GetUserProfileInputPort getUserProfileUseCase,
                RefreshTokenInputPort refreshTokenUseCase,
                LogoutInputPort logoutUseCase,
                StartTotpSetupInputPort startTotpSetupUseCase,
                EnableTotpInputPort enableTotpUseCase,
                GetTotpProvisioningUriInputPort getTotpProvisioningUriUseCase,
                AuthWebMapper authWebMapper) {
            return new AuthController(
                    loginUserUseCase,
                    getUserProfileUseCase,
                    refreshTokenUseCase,
                    logoutUseCase,
                    startTotpSetupUseCase,
                    enableTotpUseCase,
                    getTotpProvisioningUriUseCase,
                    authWebMapper
            );
        }

        @Bean
        LoginUserInputPort loginUserInputPort() {
            return Mockito.mock(LoginUserInputPort.class);
        }

        @Bean
        GetUserProfileInputPort getUserProfileInputPort() {
            return Mockito.mock(GetUserProfileInputPort.class);
        }

        @Bean
        RefreshTokenInputPort refreshTokenInputPort() {
            return Mockito.mock(RefreshTokenInputPort.class);
        }

        @Bean
        LogoutInputPort logoutInputPort() {
            return Mockito.mock(LogoutInputPort.class);
        }

        @Bean
        StartTotpSetupInputPort startTotpSetupInputPort() {
            return Mockito.mock(StartTotpSetupInputPort.class);
        }

        @Bean
        EnableTotpInputPort enableTotpInputPort() {
            return Mockito.mock(EnableTotpInputPort.class);
        }

        @Bean
        GetTotpProvisioningUriInputPort getTotpProvisioningUriInputPort() {
            return Mockito.mock(GetTotpProvisioningUriInputPort.class);
        }

        @Bean
        AuthWebMapper authWebMapper() {
            return Mockito.mock(AuthWebMapper.class);
        }
    }
}
