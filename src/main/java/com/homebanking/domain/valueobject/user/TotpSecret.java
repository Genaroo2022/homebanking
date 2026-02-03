package com.homebanking.domain.valueobject.user;

import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;

public final class TotpSecret {

    private static final String BASE32_PATTERN = "^[A-Z2-7]+=*$";
    private static final int MIN_LENGTH = 16;

    private final String value;

    private TotpSecret(String value) {
        this.value = value;
    }

    public static TotpSecret of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.TOTP_SECRET_REQUIRED);
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.length() < MIN_LENGTH || !normalized.matches(BASE32_PATTERN)) {
            throw new InvalidUserDataException(DomainErrorMessages.TOTP_SECRET_INVALID);
        }
        return new TotpSecret(normalized);
    }

    public String value() {
        return value;
    }
}
