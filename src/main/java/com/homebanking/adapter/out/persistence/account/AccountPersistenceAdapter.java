package com.homebanking.adapter.out.persistence.account;

import com.homebanking.domain.entity.Account;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class AccountPersistenceAdapter implements AccountRepository {

    private final SpringDataAccountRepository springDataAccountRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account save(Account account) {
        if (account.getId() == null) {
            AccountJpaEntity entity = accountMapper.toJpaEntity(account);
            AccountJpaEntity saved = springDataAccountRepository.save(entity);
            return accountMapper.toDomain(saved);
        }

        AccountJpaEntity existing = springDataAccountRepository.findById(account.getId())
                .orElseGet(() -> accountMapper.toJpaEntity(account));

        existing.setBalance(account.getBalance().value());

        AccountJpaEntity saved = springDataAccountRepository.save(existing);
        return accountMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return springDataAccountRepository.findById(id)
                .map(accountMapper::toDomain);
    }

    @Override
    public List<Account> findByUserId(UUID userId) {
        return springDataAccountRepository.findByUserId(userId)
                .stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Account> findByCbu(Cbu cbu) {
        return springDataAccountRepository.findByCbu(cbu.value()).map(accountMapper::toDomain);
    }

    @Override
    public boolean existsByCbu(Cbu cbu) {
        return springDataAccountRepository.existsByCbu(cbu.value());
    }
}
