
/*
 * Excepción: TransferProcessingException
 *
 * Lanzada cuando falla el procesamiento contra sistema externo.
 * Diferencia entre fallos recuperables e irrecuperables.
 */
package com.homebanking.domain.exception.transfer;

import com.homebanking.domain.exception.common.DomainException;

public class TransferProcessingException extends DomainException {

    private final boolean recoverable;
    private final String externalErrorCode;

    public TransferProcessingException(
            String message,
            boolean recoverable,
            String externalErrorCode) {
        super(message);
        this.recoverable = recoverable;
        this.externalErrorCode = externalErrorCode;
    }

    public boolean isRecoverable() {
        return recoverable;
    }

    public String getExternalErrorCode() {
        return externalErrorCode;
    }
}



