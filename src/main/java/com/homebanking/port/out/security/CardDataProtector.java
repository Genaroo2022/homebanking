package com.homebanking.port.out.security;

public interface CardDataProtector {
    String encrypt(String plainValue);
    String decrypt(String encryptedValue);
}

