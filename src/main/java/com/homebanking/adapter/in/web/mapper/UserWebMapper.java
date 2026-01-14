package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.RegisterUserRequest;
import com.homebanking.domain.entity.User;
import org.springframework.stereotype.Component;

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
}