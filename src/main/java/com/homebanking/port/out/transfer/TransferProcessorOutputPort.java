
/*
 * Output Port: TransferProcessorOutputPort

 * Abstracción para procesar transferencias (integración externa).
 * Permite múltiples implementaciones: mock, real API, etc.
 */
package com.homebanking.port.out.transfer;

import com.homebanking.domain.entity.Transfer;

public interface TransferProcessorOutputPort {

    /**
     * Procesa una transferencia contra sistema externo.
     *
     * @param transfer Transferencia a procesar
     * @return true si fue procesada exitosamente, false en caso contrario
     * @throws com.homebanking.domain.exception.transfer.TransferProcessingException Si error irrecuperable
     */
    boolean processTransfer(Transfer transfer);
}



