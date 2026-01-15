package com.homebanking.adapter.out.persistence.user;

import com.homebanking.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
class UserMapper {

    UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getDni(),
                user.getBirthDate(),
                user.getAddress(),
                user.getCreatedAt()
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
                entity.getCreatedAt()
        );
    }
}