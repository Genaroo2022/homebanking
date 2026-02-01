package com.homebanking.port.in.transfer;


import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

/**
 * Input Port: CreateTransferInputPort

 * Contrato para el caso de uso de crear transferencias.
 * Define la interfaz que debe implementar el "use case".

 * No conoce detalles técnicos de HTTP, persistencia, etc.
 */
public interface CreateTransferInputPort {

    /**
     * Crea una nueva transferencia.

     * Garantías:
     * - Idempotente: múltiples llamadas con mismo idempotencyKey producen un resultado
     * - Atómico: o la transferencia se crea completa o no se crea
     * - Transaccional: cambios en BD ocurren juntos

     * @param request Datos de la transferencia (incluyendo idempotencyKey)
     * @return Transferencia creada con ID asignado
     * @throws com.homebanking.domain.exception.transfer.InvalidTransferDataException Si datos son inválidos
     * @throws com.homebanking.domain.exception.account.InvalidAccountDataException Si cuenta no existe
     * @throws com.homebanking.domain.exception.account.InsufficientFundsException Si no hay saldo
     */
    TransferOutputResponse createTransfer(CreateTransferInputRequest request);
}




