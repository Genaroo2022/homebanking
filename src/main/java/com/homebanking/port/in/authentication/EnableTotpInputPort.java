package com.homebanking.port.in.authentication;

public interface EnableTotpInputPort {
    void enable(String email, String code);
}
