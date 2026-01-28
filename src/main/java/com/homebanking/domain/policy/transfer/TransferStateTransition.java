package com.homebanking.domain.policy.transfer;

import com.homebanking.domain.entity.Transfer;

public interface TransferStateTransition {
    enum Type {
        TAKE_FOR_PROCESSING,
        MARK_AS_COMPLETED,
        MARK_AS_FAILED,
        MARK_AS_REJECTED,
        PREPARE_FOR_RETRY
    }
    /**
     * Ejecuta la transición de estado.
     * Este es un CONTRATO DE DOMINIO, no un servicio.
     */
    void execute(Transfer transfer);
    boolean isApplicable(Transfer transfer);
}
