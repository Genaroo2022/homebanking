package com.homebanking.adapter.out.external.payment.adapter;

import com.homebanking.adapter.out.external.payment.client.TransferProcessorClient;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.TransferProcessingException;
import com.homebanking.port.out.transfer.TransferProcessorOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Random;

/**
 * Adapter: TransferProcessorAdapter

 * Implementación del puerto TransferProcessorOutputPort.
 * Actúa como puente entre dominio y servicio de procesamiento externo.

 * Responsabilidades:
 * ✓ Llamar al cliente externo (API, servicio bancario, etc.)
 * ✓ Manejar timeouts
 * ✓ Distinguir entre fallos recuperables e irrecuperables
 * ✓ Logging de integración

 * Resiliencia:
 * • Timeouts bien definidos
 * • Reintentos en capas superiores (TransferProcessorService)
 * • Circuit breaker (podría implementarse con "Resilience 4j")
 * • Graceful degradation si el servicio externo está fuera
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferProcessorAdapter implements TransferProcessorOutputPort {

    private final TransferProcessorClient transferProcessorClient;
    private final Random random = new Random();

    /**
     * Procesa una transferencia contra el sistema externo.
     *
     * @param transfer Transferencia a procesar
     * @return true si fue procesada exitosamente, false si fallo recuperable
     * @throws TransferProcessingException Si error irrecuperable
     */
    @Override
    public boolean processTransfer(Transfer transfer) {
        log.info("Procesando transferencia en sistema externo: id={}, targetCbu={}, amount={}",
                transfer.getId(), transfer.getTargetCbu().value(), transfer.getAmount().value());

        try {
            // Llamar al cliente externo con timeout
            boolean result = transferProcessorClient.submitTransfer(
                    transfer.getId(),
                    transfer.getOriginAccountId(),
                    transfer.getTargetCbu().value(),
                    transfer.getAmount().value(),
                    transfer.getDescription().value(),
                    transfer.getIdempotencyKey().value()
            );

            log.debug("Procesamiento externo completado: id={}, result={}",
                    transfer.getId(), result);

            return result;

        } catch (TransferProcessorClient.TemporaryException ex) {
            // Error temporal: red lenta, timeout, etc.
            // Debe ser reintentado
            log.warn("Error temporal procesando transferencia: id={}, {}",
                    transfer.getId(), ex.getMessage());

            throw new TransferProcessingException(
                    "Error temporal al procesar transferencia: " + ex.getMessage(),
                    true,  // recoverable = true
                    "TEMPORARY_ERROR"
            );

        } catch (TransferProcessorClient.PermanentException ex) {
            // Error permanente: CBU inválido, datos rechazados, etc.
            // NO debe ser reintentado
            log.error("Error permanente procesando transferencia: id={}, {}",
                    transfer.getId(), ex.getMessage());

            throw new TransferProcessingException(
                    "Error al procesar transferencia: " + ex.getMessage(),
                    false,  // recoverable = false
                    ex.getErrorCode()
            );
        }
    }
}



