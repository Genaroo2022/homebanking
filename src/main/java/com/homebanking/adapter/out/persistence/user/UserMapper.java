package com.homebanking.adapter.out.persistence.user;

import com.homebanking.domain.entity.User;
import com.homebanking.domain.enums.TotpStatus;
import org.springframework.stereotype.Component;

@Component
class UserMapper {

    UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getName().value(),
                user.getLastName().value(),
                user.getEmail().value(),
                user.getPassword().value(),
                user.getDni().value(),
                user.getBirthDate().value(),
                user.getAddress().value(),
                user.getCreatedAt(),
                user.getTotpSecret() == null ? null : user.getTotpSecret().value(),
                user.getTotpStatus() == TotpStatus.ENABLED
        );

        if (user.getId() != null) {
            entity.setId(user.getId());
        }

        return entity;
    }

    public User toDomainEntity(UserJpaEntity entity) {
        return User.withId(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getName(),
                entity.getLastName(),
                entity.getDni(),
                entity.getBirthDate(),
                entity.getAddress(),
                entity.getCreatedAt(),
                entity.getTotpSecret(),
                entity.isTotpEnabled()
        );
    }
}


