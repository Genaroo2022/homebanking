package com.homebanking.config;

import com.homebanking.application.mapper.TransferMapper;
import com.homebanking.application.service.transfer.TransferBatchProcessingService;
import com.homebanking.application.service.transfer.TransferStateTransitionService;
import com.homebanking.application.usecase.transfer.CreateTransferUseCaseImpl;
import com.homebanking.application.usecase.transfer.GetTransferUseCaseImpl;
import com.homebanking.application.usecase.transfer.ProcessTransferUseCaseImpl;
import com.homebanking.application.usecase.transfer.RetryFailedTransferUseCaseImpl;
import com.homebanking.port.in.transfer.CreateTransferInputPort;
import com.homebanking.port.in.transfer.GetTransferInputPort;
import com.homebanking.port.in.transfer.ProcessTransferInputPort;
import com.homebanking.port.in.transfer.RetryTransferInputPort;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.EventPublisher;
import com.homebanking.port.out.TransferProcessorOutputPort;
import com.homebanking.port.out.TransferRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Config: TransferConfig

 * Configuracion de inyeccion de dependencias para transferencias.

 * Responsabilidades:
 * ? "@Instanciar" "use cases"
 * ? Inyectar dependencias (repositories, ports)
 * ? Mantener arbol de dependencias limpio

 * Beneficios:
 * - Centraliza configuracion
 * - Facilita testing ("@mockear" dependencias)
 * - Documenta la estructura de dependencias
 * - Permite multiples implementaciones del mismo puerto
 */
@Configuration
public class TransferConfig {

    /**
     * Bean: CreateTransferInputPort

     * Instancia el "use case" de crear transferencias.
     * Inyecta sus dependencias: repositories y ports.
     */
    @Bean
    public CreateTransferInputPort createTransferUseCase(
            AccountRepository accountRepository,
            TransferRepository transferRepository,
            EventPublisher eventPublisher) {
        return new CreateTransferUseCaseImpl(
                accountRepository,
                transferRepository,
                eventPublisher
        );
    }

    @Bean
    public GetTransferInputPort getTransferUseCase(TransferRepository transferRepository) {
        return new GetTransferUseCaseImpl(transferRepository);
    }

    /**
     * Bean: TransferProcessorService

     * Servicio que procesa transferencias de forma asincronica.
     * Inyecta:
     * - Repositories (para leer/escribir estado)
     * - TransferProcessorOutputPort (para llamar sistema externo)
     * - NotificationOutputPort (para notificar usuarios)
     */
    @Bean
    public ProcessTransferInputPort processTransferUseCase(
            TransferProcessorOutputPort transferProcessor,
            TransferMapper transferMapper,
            TransferStateTransitionService stateService) {
        return new ProcessTransferUseCaseImpl(
                transferProcessor,
                transferMapper,
                stateService
        );
    }

    @Bean
    public RetryTransferInputPort retryFailedTransferUseCase(
            TransferRepository transferRepository,
            ProcessTransferInputPort processTransferUseCase) {
        return new RetryFailedTransferUseCaseImpl(
                transferRepository,
                processTransferUseCase
        );
    }

    @Bean
    public TransferBatchProcessingService transferBatchProcessingService(
            TransferRepository transferRepository,
            ProcessTransferInputPort processTransferUseCase,
            RetryTransferInputPort retryFailedTransferUseCase) {
        return new TransferBatchProcessingService(
                transferRepository,
                processTransferUseCase,
                retryFailedTransferUseCase
        );
    }
}


