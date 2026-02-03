package com.homebanking.adapter.in.web.security;

import com.homebanking.port.out.auth.AccessTokenStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
    private final UserDetailsService userDetailsService;
    private final AccessTokenStore accessTokenStore;

    @Override
    protected void doFilterInternal(
            @lombok.NonNull HttpServletRequest request,
            @lombok.NonNull HttpServletResponse response,
            @lombok.NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No hay token JWT en la solicitud: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            if (jwtService.validateToken(jwt) && jwtService.isAccessToken(jwt)) {
                if (accessTokenStore.isBlacklisted(jwt)) {
                    log.warn("Token JWT revocado en la solicitud: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }
                userEmail = jwtService.extractUsername(jwt);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                    if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } else {
                log.warn("Token JWT inválido en la solicitud: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            log.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}


