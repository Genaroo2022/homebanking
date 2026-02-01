package com.homebanking.port.out.auth;

/**
 * Output Port: TokenGenerator
 *
 * Abstraccion para generar tokens de autenticacion.
 */
public interface TokenGenerator {
    String generateToken(String subject);
}



