
package com.homebanking.domain.service;

import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.InsufficientFundsException;
import com.homebanking.domain.exception.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Domain Service para lógica compleja que involucra múltiples entidades

 * Diferencia entre Entity y DomainService:
 * - Entity: Lógica de una entidad
 * - DomainService: Lógica que involucra MÚLTIPLES entidades

 * En este caso: Transfer involve source y destination accounts
 */
@RequiredArgsConstructor
public class TransferDomainService {

    /**
     * Ejecuta la lógica de debitar cuenta origen y acreditar destino

     * Esta lógica NO va en TransferMoneyUseCase (eso sería orquestación)
     * Va aquí porque es regla de negocio pura

     * Precondiciones validadas:
     * - source!= null
     * - destination!= null
     * - amount > 0
     * - source.balance >= amount

     * Postcondiciones garantizadas:
     * - source.balance decreased
     * - destination.balance increased
     * - Ambas cuentas modificadas
     *
     * @throws InsufficientFundsException si source no tiene fondos
     */
    public void executeTransfer(
            Account source,
            Account destination,
            Transfer transfer) {

        // Validaciones previas (precondiciones)
        if (source.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS,source.getId(),
                    transfer.getAmount(),
                    source.getBalance());
        }

        if (source.getId().equals(destination.getId())) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.TRANSFER_SAME_ACCOUNT);
        }

        // Ejecutar transfer (ambas operaciones juntas)
        source.debit(transfer.getAmount());
        destination.deposit(transfer.getAmount());

        // Postcondiciones: Balances were updated
        // assert source.getBalance().compareTo(oldSourceBalance - amount) == 0
        // assert destination.getBalance().compareTo(oldDestBalance + amount) == 0
    }

    /**
     * Simula una transferencia (sin persistir cambios)

     * Útil para:
     * - Validar transferencia antes de ejecutar
     * - Calcular fees o impuestos
     * - Preview de operación
     */
    public void simulateTransfer(
            Account source,
            Account destination,
            BigDecimal amount) {

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS, source.getId(),
                    amount,
                    source.getBalance());
        }

        // Simulate changes (no persistence)
        BigDecimal simulatedSourceBalance =
                source.getBalance().subtract(amount);
        BigDecimal simulatedDestBalance =
                destination.getBalance().add(amount);

        // No assert, solo confirmamos que sería válido
        if (simulatedSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS, source.getId(),
                    amount,
                    source.getBalance());
        }
    }
}
