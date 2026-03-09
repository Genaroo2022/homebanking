package com.homebanking.config;

import com.homebanking.application.mapper.BillPaymentMapper;
import com.homebanking.application.usecase.payment.GetBillPaymentUseCaseImpl;
import com.homebanking.application.usecase.payment.PayBillUseCaseImpl;
import com.homebanking.port.in.payment.GetBillPaymentInputPort;
import com.homebanking.port.in.payment.PayBillInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.payment.BillPaymentRepository;
import com.homebanking.port.out.payment.BillProcessorOutputPort;
import com.homebanking.port.out.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Bean
    public PayBillInputPort payBillInputPort(
            BillPaymentRepository billPaymentRepository,
            BillProcessorOutputPort billProcessorOutputPort,
            AccountRepository accountRepository,
            UserRepository userRepository,
            BillPaymentMapper billPaymentMapper) {
        return new PayBillUseCaseImpl(
                billPaymentRepository,
                billProcessorOutputPort,
                accountRepository,
                userRepository,
                billPaymentMapper
        );
    }

    @Bean
    public GetBillPaymentInputPort getBillPaymentInputPort(
            BillPaymentRepository billPaymentRepository,
            UserRepository userRepository,
            AccountRepository accountRepository,
            BillPaymentMapper mapper) {
        return new GetBillPaymentUseCaseImpl(
                billPaymentRepository,
                userRepository,
                accountRepository,
                mapper
        );
    }
}

