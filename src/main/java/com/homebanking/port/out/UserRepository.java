package com.homebanking.port.out;

import com.homebanking.domain.entity.User;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    // Optimización: Buscamos por cualquiera de los dos campos únicos
    // Nota: Esto devolverá un usuario si CUALQUIERA de los dos coincide.
    Optional<User> findByEmailOrDni(String email, String dni);
}