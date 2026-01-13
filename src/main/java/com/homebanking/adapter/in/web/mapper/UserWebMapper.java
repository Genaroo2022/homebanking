package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.RegisterUserRequest;
import com.homebanking.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public User toDomain(RegisterUserRequest request) {
        // Aquí ocurre la magia:
        // Al llamar al "new User(...)", se disparan todas tus validaciones de Dominio (Regex, Edad, etc).
        // Si el DTO trae datos basura (ej: DNI con letras), el constructor de User lanzará la excepción.
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