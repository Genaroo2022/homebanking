
/*
 * Excepción: SameAccountTransferException
 *
 * Lanzada cuando se intenta transferir de una cuenta a sí misma.
 */
package com.homebanking.domain.exception;

public class SameAccountTransferException extends DomainException {
    public SameAccountTransferException(String message) {
        super(message);
    }
}