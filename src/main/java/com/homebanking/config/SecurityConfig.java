package com.homebanking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.homebanking.adapter.in.web.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Rutas de Negocio
    private static final String USERS_REGISTER = "/users";
    private static final String AUTH_LOGIN = "/auth/login";

    // Rutas de Infraestructura / Documentación (La "Lista Blanca")
    private static final String[] PUBLIC_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/h2-console/**"
    };
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // 1. Permitir endpoints de negocio (Registro y Login)
                    auth.requestMatchers(HttpMethod.POST, USERS_REGISTER).permitAll();
                    auth.requestMatchers(HttpMethod.POST, AUTH_LOGIN).permitAll();

                    // 2. Permitir toda la lista de URLs públicas (Swagger, H2)
                    auth.requestMatchers(PUBLIC_URLS).permitAll();

                    // 3. Todo lo demás requiere autenticación
                    auth.anyRequest().authenticated();
                })
                // Permitir frames para H2 Console
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}