
package com.homebanking.config;

import com.homebanking.application.service.transfer.ProcessTransferApplicationService;
import com.homebanking.application.usecase.transfer.CreateTransferUseCase;
import com.homebanking.application.usecase.transfer.CreateTransferUseCaseImpl;
import com.homebanking.application.usecase.transfer.GetTransferUseCase;
import com.homebanking.application.usecase.transfer.GetTransferUseCaseImpl;
import com.homebanking.application.usecase.transfer.ProcessTransferUseCase;
import com.homebanking.application.usecase.transfer.ProcessTransferUseCaseImpl;
import com.homebanking.application.usecase.transfer.RetryFailedTransferUseCase;
import com.homebanking.application.usecase.transfer.RetryFailedTransferUseCaseImpl;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.NotificationOutputPort;
import com.homebanking.port.out.TransferProcessorOutputPort;
import com.homebanking.port.out.TransferRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Config: TransferConfig

 * Configuración de inyección de dependencias para transferencias.

 * Responsabilidades:
 * ✓ "@Instanciar" "use cases"
 * ✓ Inyectar dependencias (repositories, ports)
 * ✓ Mantener árbol de dependencias limpio

 * Beneficios:
 * • Centraliza configuración
 * • Facilita testing ("@mockear" dependencias)
 * • Documenta la estructura de dependencias
 * • Permite múltiples implementaciones del mismo puerto
 */
@Configuration
public class TransferConfig {

    /**
     * Bean: CreateTransferInputPort

     * Instancia el "use case" de crear transferencias.
     * Inyecta sus dependencias: repositories y ports.
     */
    @Bean
    public CreateTransferUseCase createTransferUseCase(
            AccountRepository accountRepository,
            TransferRepository transferRepository) {
        return new CreateTransferUseCaseImpl(
                accountRepository,
                transferRepository
        );
    }

    @Bean
    public GetTransferUseCase getTransferUseCase(TransferRepository transferRepository) {
        return new GetTransferUseCaseImpl(transferRepository);
    }

    /**
     * Bean: TransferProcessorService

     * Servicio que procesa transferencias de forma asincrónica.
     * Inyecta:
     * • Repositories (para leer/escribir estado)
     * • TransferProcessorOutputPort (para llamar sistema externo)
     * • NotificationOutputPort (para notificar usuarios)
     */
    @Bean
    public ProcessTransferUseCase processTransferUseCase(
            TransferRepository transferRepository,
            AccountRepository accountRepository,
            TransferProcessorOutputPort transferProcessor,
            NotificationOutputPort notificationPort) {
        return new ProcessTransferUseCaseImpl(
                transferRepository,
                accountRepository,
                transferProcessor,
                notificationPort
        );
    }

    @Bean
    public RetryFailedTransferUseCase retryFailedTransferUseCase(
            TransferRepository transferRepository,
            ProcessTransferUseCase processTransferUseCase) {
        return new RetryFailedTransferUseCaseImpl(
                transferRepository,
                processTransferUseCase
        );
    }

    @Bean
    public ProcessTransferApplicationService transferProcessorService(
            TransferRepository transferRepository,
            ProcessTransferUseCase processTransferUseCase,
            RetryFailedTransferUseCase retryFailedTransferUseCase) {
        return new ProcessTransferApplicationService(
                transferRepository,
                processTransferUseCase,
                retryFailedTransferUseCase
        );
    }
}
