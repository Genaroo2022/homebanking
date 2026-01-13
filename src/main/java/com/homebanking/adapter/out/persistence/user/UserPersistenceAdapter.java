package com.homebanking.adapter.out.persistence.user;

import com.homebanking.domain.entity.User;
import com.homebanking.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor // Lombok genera el constructor para inyectar las dependencias
class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        // 1. Traducir Dominio -> Entidad JPA
        UserJpaEntity entityToSave = userMapper.toJpaEntity(user);

        // 2. Guardar usando Spring Data (devuelve la entidad con ID nuevo)
        UserJpaEntity savedEntity = springDataUserRepository.save(entityToSave);

        // 3. Traducir de vuelta JPA -> Dominio (para devolver el usuario con ID)
        return userMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<User> findByEmailOrDni(String email, String dni) {
        // Buscamos, y si encontramos algo, lo mapeamos a Dominio al vuelo
        return springDataUserRepository.findByEmailOrDni(email, dni)
                .map(userMapper::toDomainEntity);
    }
}