package com.homebanking.application.usecase.payment;

import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;
import com.homebanking.application.mapper.BillPaymentMapper;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.BillPayment;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.payment.BillPaymentNotFoundException;
import com.homebanking.domain.exception.security.AccessDeniedException;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.payment.GetBillPaymentInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.payment.BillPaymentRepository;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetBillPaymentUseCaseImpl implements GetBillPaymentInputPort {

    private final BillPaymentRepository billPaymentRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BillPaymentMapper mapper;

    @Override
    public BillPaymentOutputResponse getById(UUID paymentId, String requesterEmail) {
        BillPayment payment = billPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new BillPaymentNotFoundException(
                        DomainErrorMessages.BILL_PAYMENT_NOT_FOUND,
                        paymentId
                ));
        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND));
        Account account = accountRepository.findById(payment.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        payment.getAccountId()
                ));

        if (!account.getUserId().equals(user.getId())) {
            throw new AccessDeniedException(DomainErrorMessages.ACCESS_DENIED);
        }
        return mapper.toDto(payment);
    }
}

