package com.homebanking.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Inyectamos nuestro CustomUserDetailsService

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String userEmail;

        // 1. Validar si el header existe y tiene el formato correcto "Bearer ..."
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No hay token JWT en la solicitud: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token (quitando la palabra "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // 3. Extraer el email del token
            userEmail = jwtService.extractUsername(jwt);

            // 4. Si hay email y el usuario no está autenticado todavía en el contexto
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Cargamos los detalles del usuario desde la BD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 6. Validamos el token contra el usuario y su expiración
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                    // 7. Creamos el objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 8. ¡MÁGIA! Ponemos al usuario en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
            // No lanzamos error aquí, dejamos que la cadena siga.
            // Si el endpoint requería seguridad, el "Portero" lo rechazará más adelante.
        }

        filterChain.doFilter(request, response);
    }
}