
/*
 * Output Port: NotificationOutputPort

 * Abstracción para notificaciones externas.
 * Permite desacoplar la lógica de negocio de canales específicos.
 */
package com.homebanking.port.out;

import com.homebanking.domain.entity.Transfer;

public interface NotificationOutputPort {

    /**
     * Notifica que una transferencia fue completada.
     * Puede ser email, SMS, push, etc.
     */
    void notifyTransferCompleted(Transfer transfer);

    /**
     * Notifica que una transferencia falló.
     */
    void notifyTransferFailed(Transfer transfer);
}