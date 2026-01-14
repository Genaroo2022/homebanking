package com.homebanking.adapter.out.persistence.user;

import com.homebanking.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
class UserMapper {

    UserJpaEntity toJpaEntity(User user) {
        return new UserJpaEntity(
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getDni(),
                user.getBirthDate(),
                user.getAddress(),
                user.getCreatedAt()
        );
    }

    UserJpaEntity toJpaEntityWithId(User user) {
        return UserJpaEntity.withId(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getDni(),
                user.getBirthDate(),
                user.getAddress(),
                user.getCreatedAt()
        );
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
                entity.getCreatedAt()
        );
    }
}