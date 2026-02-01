package com.homebanking.domain.exception.transfer;


import com.homebanking.domain.exception.common.DomainException;
/**
 * Excepcion: DestinationAccountNotFoundException
 *
 * Lanzada cuando no se encuentra la cuenta destino.
 */
public class DestinationAccountNotFoundException extends DomainException {

    private final String targetCbu;

    public DestinationAccountNotFoundException(String message, String targetCbu) {
        super(message);
        this.targetCbu = targetCbu;
    }

    public String getTargetCbu() {
        return targetCbu;
    }
}




