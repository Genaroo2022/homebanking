package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.LoginRequest;
import com.homebanking.adapter.in.web.response.TokenResponse;
import com.homebanking.adapter.in.web.response.MeResponse;
import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.application.dto.profile.response.UserProfileOutputResponse;
import com.homebanking.application.dto.profile.response.UserProfileOutputResponse.AccountOutputResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthWebMapper {

    public LoginInputRequest toInputRequest(LoginRequest request, String ipAddress) {
        return new LoginInputRequest(
                request.getEmail(),
                request.getPassword(),
                ipAddress
        );
    }

    public TokenResponse toResponse(TokenOutputResponse output) {
        return new TokenResponse(output.token());
    }

    public MeResponse toMeResponse(UserProfileOutputResponse output) {
        return MeResponse.builder()
                .id(output.id())
                .email(output.email())
                .name(output.name())
                .lastName(output.lastName())
                .accounts(toAccountDtoList(output.accounts()))
                .build();
    }

    private List<MeResponse.AccountDto> toAccountDtoList(
            List<AccountOutputResponse> accounts) {
        return accounts.stream()
                .map(acc -> MeResponse.AccountDto.builder()
                        .id(acc.id())
                        .cbu(acc.cbu())
                        .alias(acc.alias())
                        .balance(acc.balance())
                        .build())
                .toList();
    }
}
