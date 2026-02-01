package com.homebanking.adapter.in.web.security;

import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@NullMarked
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.homebanking.domain.entity.User domainUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento de acceso con usuario no encontrado: {}", email);
                    return new UsernameNotFoundException(DomainErrorMessages.USER_NOT_FOUND);
                });
        log.debug("UserDetails cargado para: {}", email);
        return User.withUsername(domainUser.getEmail().value())
                .password(domainUser.getPassword().value())
                .authorities(Collections.emptyList())
                .build();
    }
}


