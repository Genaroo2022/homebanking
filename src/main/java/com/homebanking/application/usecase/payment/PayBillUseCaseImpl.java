package com.homebanking.application.usecase.payment;

import com.homebanking.application.dto.payment.request.PayBillInputRequest;
import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;
import com.homebanking.application.mapper.BillPaymentMapper;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.BillPayment;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.account.InsufficientFundsException;
import com.homebanking.domain.exception.security.AccessDeniedException;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.payment.PayBillInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.payment.BillPaymentRepository;
import com.homebanking.port.out.payment.BillProcessorOutputPort;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
public class PayBillUseCaseImpl implements PayBillInputPort {

    private final BillPaymentRepository billPaymentRepository;
    private final BillProcessorOutputPort billProcessorOutputPort;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BillPaymentMapper mapper;

    @Override
    @Transactional
    public BillPaymentOutputResponse pay(PayBillInputRequest request) {
        Optional<BillPayment> existing = billPaymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return mapper.toDto(existing.get());
        }

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        request.accountId()
                ));
        validateOwnership(account, request.requesterEmail());

        BillPayment payment = BillPayment.create(
                request.accountId(),
                request.billerCode(),
                request.reference(),
                request.amount(),
                request.idempotencyKey()
        );
        billPaymentRepository.save(payment);

        try {
            boolean accepted = billProcessorOutputPort.process(payment);
            if (accepted) {
                account.debit(request.amount());
                payment.markAsPaid();
                accountRepository.save(account);
            } else {
                payment.markAsFailed("Pago rechazado por procesador externo");
            }
        } catch (InsufficientFundsException ex) {
            payment.markAsFailed(ex.getMessage());
        } catch (RuntimeException ex) {
            payment.markAsFailed("Error temporal del procesador externo");
        }

        BillPayment saved = billPaymentRepository.save(payment);
        return mapper.toDto(saved);
    }

    private void validateOwnership(Account account, String requesterEmail) {
        if (requesterEmail == null || requesterEmail.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND);
        }
        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND));
        if (!account.getUserId().equals(user.getId())) {
            throw new AccessDeniedException(DomainErrorMessages.ACCESS_DENIED);
        }
    }
}

