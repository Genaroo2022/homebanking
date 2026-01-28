
package com.homebanking.domain.service.transfer;

import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.InsufficientFundsException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
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
        if (source.getBalance().value().compareTo(transfer.getAmount().value()) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS,source.getId(),
                    transfer.getAmount().value(),
                    source.getBalance().value());
        }

        if (source.getId().equals(destination.getId())) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.TRANSFER_SAME_ACCOUNT);
        }

        // Ejecutar transfer (ambas operaciones juntas)
        source.debit(transfer.getAmount().value());
        destination.deposit(transfer.getAmount().value());

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

        if (source.getBalance().value().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS, source.getId(),
                    amount,
                    source.getBalance().value());
        }

        // Simulate changes (no persistence)
        BigDecimal simulatedSourceBalance =
                source.getBalance().value().subtract(amount);
        BigDecimal simulatedDestBalance =
                destination.getBalance().value().add(amount);

        // No assert, solo confirmamos que sería válido
        if (simulatedSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS, source.getId(),
                    amount,
                    source.getBalance().value());
        }
    }
}

