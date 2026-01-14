package com.homebanking.adapter.out.persistence.user;

import com.homebanking.domain.entity.User;
import com.homebanking.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserJpaEntity entityToSave = userMapper.toJpaEntity(user);

        UserJpaEntity savedEntity = springDataUserRepository.save(entityToSave);

        return userMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userMapper::toDomainEntity);
    }

    @Override
    public Optional<User> findByEmailOrDni(String email, String dni) {
        return springDataUserRepository.findByEmailOrDni(email, dni)
                .map(userMapper::toDomainEntity);
    }
}