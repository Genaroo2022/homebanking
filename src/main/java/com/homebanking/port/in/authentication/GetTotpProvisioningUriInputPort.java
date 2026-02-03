package com.homebanking.port.in.authentication;

public interface GetTotpProvisioningUriInputPort {
    String getProvisioningUri(String email);
}
