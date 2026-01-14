package com.homebanking.port.out;

import com.homebanking.domain.entity.User;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrDni(String email, String dni);
}