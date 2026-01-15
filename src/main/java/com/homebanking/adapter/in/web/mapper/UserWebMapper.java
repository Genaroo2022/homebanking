package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.RegisterUserRequest;
import com.homebanking.adapter.in.web.response.MeResponse;
import com.homebanking.domain.entity.User;
import org.springframework.stereotype.Component;
import com.homebanking.application.dto.response.UserProfileOutput;

import java.util.List;

@Component
public class UserWebMapper {

    public User toDomain(RegisterUserRequest request) {
        return new User(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getLastName(),
                request.getDni(),
                request.getBirthDate(),
                request.getAddress()
        );
    }

    public MeResponse toMeResponse(UserProfileOutput output) {
        return MeResponse.builder()
                .id(output.id())
                .email(output.email())
                .name(output.name())
                .lastName(output.lastName())
                .accounts(toAccountDtoList(output.accounts()))
                .build();
    }

    private List<MeResponse.AccountDto> toAccountDtoList(List<UserProfileOutput.AccountOutput> accounts) {
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