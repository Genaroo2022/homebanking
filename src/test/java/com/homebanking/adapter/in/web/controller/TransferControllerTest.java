package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.TransferWebMapper;
import com.homebanking.adapter.in.web.request.CreateTransferRequest;
import com.homebanking.port.in.transfer.CreateTransferInputPort;
import com.homebanking.port.in.transfer.GetTransferInputPort;
import com.homebanking.port.in.transfer.ProcessTransferInputPort;
import com.homebanking.port.in.transfer.RetryTransferInputPort;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TransferControllerTest.TestConfig.class)
class TransferControllerTest {

    @Autowired
    private TransferController transferController;

    @Test
    void shouldFailValidation_WhenIdempotencyKeyHeaderIsMissing() {
        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                "1234567890123456789012",
                new BigDecimal("100.50"),
                "Pago de servicios"
        );

        assertThrows(ConstraintViolationException.class,
                () -> transferController.createTransfer(null, request));
    }

    @Configuration
    static class TestConfig {

        @Bean
        TransferController transferController(
                CreateTransferInputPort createTransferUseCase,
                GetTransferInputPort getTransferUseCase,
                ProcessTransferInputPort processTransferUseCase,
                RetryTransferInputPort retryFailedTransferUseCase,
                TransferWebMapper transferWebMapper) {
            return new TransferController(
                    createTransferUseCase,
                    getTransferUseCase,
                    processTransferUseCase,
                    retryFailedTransferUseCase,
                    transferWebMapper
            );
        }

        @Bean
        CreateTransferInputPort createTransferInputPort() {
            return Mockito.mock(CreateTransferInputPort.class);
        }

        @Bean
        GetTransferInputPort getTransferInputPort() {
            return Mockito.mock(GetTransferInputPort.class);
        }

        @Bean
        ProcessTransferInputPort processTransferInputPort() {
            return Mockito.mock(ProcessTransferInputPort.class);
        }

        @Bean
        RetryTransferInputPort retryTransferInputPort() {
            return Mockito.mock(RetryTransferInputPort.class);
        }

        @Bean
        TransferWebMapper transferWebMapper() {
            return Mockito.mock(TransferWebMapper.class);
        }

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
            MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
            processor.setValidator(validator);
            return processor;
        }
    }
}
