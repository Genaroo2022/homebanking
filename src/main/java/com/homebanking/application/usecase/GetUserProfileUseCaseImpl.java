package com.homebanking.application.usecase;

import com.homebanking.application.dto.profile.request.GetUserProfileInputRequest;
import com.homebanking.application.dto.profile.response.UserProfileOutputResponse;
import com.homebanking.application.dto.profile.response.UserProfileOutputResponse.AccountOutputResponse;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.authentication.GetUserProfileInputPort;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso para obtener perfil de usuario.

 * Responsabilidades:
 * - Recuperar usuario del repositorio
 * - Recuperar todas las cuentas asociadas al usuario
 * - Mapear entidades de dominio a DTOs de salida
 */
@RequiredArgsConstructor
public class GetUserProfileUseCaseImpl implements GetUserProfileInputPort {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    /**
     * Obtiene el perfil completo del usuario con todas sus cuentas.
     *
     * @param request Email del usuario autenticado
     * @return Perfil del usuario con cuentas asociadas
     * @throws InvalidUserDataException Si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileOutputResponse getUserProfile(GetUserProfileInputRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidUserDataException(
                        DomainErrorMessages.USER_NOT_FOUND));

        var accounts = accountRepository.findByUserId(user.getId());

        var accountOutputs = accounts.stream()
                .map(acc -> new AccountOutputResponse(
                        acc.getId(),
                        acc.getCbu(),
                        acc.getAlias(),
                        acc.getBalance()
                ))
                .toList();

        return new UserProfileOutputResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getLastName(),
                accountOutputs
        );
    }
}
