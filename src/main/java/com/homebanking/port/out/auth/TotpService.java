package com.homebanking.port.out.auth;

public interface TotpService {
    String generateSecret();
    String buildProvisioningUri(String issuer, String accountName, String secret);
    boolean verifyCode(String secret, String code);
}
