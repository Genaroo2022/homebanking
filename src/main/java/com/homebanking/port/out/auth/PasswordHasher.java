package com.homebanking.port.out.auth;

/**
 * Output Port: PasswordHasher

 * Abstraccion para hash/verificacion de contrasenas.
 */
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}



