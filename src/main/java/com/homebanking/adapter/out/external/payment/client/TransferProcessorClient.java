
/*
 * Client: TransferProcessorClient
 *
 * Interfaz de comunicación con servicio externo.
 * En producción, sería una llamada HTTP a banco central, Banco Nación, etc.
 *
 * Patrón: Integración externa con manejo de errores explícito.
 */
package com.homebanking.adapter.out.external.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import java.math.BigDecimal;
import java.util.Random;

/**
 * Mock Implementation de TransferProcessorClient

 * En desarrollo/testing, simula respuestas del sistema externo.
 * En producción, sería reemplazado por implementación real (HTTP client).

 * Para testing:
 * • 80% éxito
 * • 10% error temporal (timeout, red lenta)
 * • 10% error permanente (datos rechazados)
 */
@Component
@Slf4j
public class TransferProcessorClient {

    private static final Random random = new Random();

    /**
     * Envía una transferencia al procesador externo.
     *
     * @return true si fue aceptada, false si rechazada pero recuperable
     * @throws TemporaryException Si error temporal (reintentable)
     * @throws PermanentException Si error permanente (no reintentable)
     */
    public boolean submitTransfer(
            Long transferId,
            Long originAccountId,
            String targetCbu,
            BigDecimal amount,
            String description,
            String idempotencyKey) throws TemporaryException, PermanentException {

        log.debug("Llamando servicio externo para transferencia: id={}", transferId);

        // Simulación de comportamiento del sistema externo
        int outcome = random.nextInt(100);

        // 80% éxito
        if (outcome < 80) {
            log.info("Transferencia aceptada por sistema externo: id={}", transferId);
            return true;
        }

        // 10% error temporal
        if (outcome < 90) {
            log.warn("Error temporal del sistema externo: id={}", transferId);
            throw new TemporaryException(
                    "Timeout al conectar con servidor externo. Reintentando..."
            );
        }

        // 10% error permanente
        log.error("Transferencia rechazada permanentemente: id={}", transferId);
        throw new PermanentException(
                "CBU de destino inválido o insuficiente",
                "INVALID_CBU"
        );
    }

    /**
     * Excepción para errores temporales (reintentables).
     */
    public static class TemporaryException extends Exception {
        public TemporaryException(String message) {
            super(message);
        }

        public TemporaryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Excepción para errores permanentes (no reintentables).
     */
    public static class PermanentException extends Exception {
        private final String errorCode;

        public PermanentException(String message, String errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}