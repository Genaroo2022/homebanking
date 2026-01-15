package com.homebanking.adapter.out.persistence.account;

import com.homebanking.domain.entity.Account;
import com.homebanking.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class AccountPersistenceAdapter implements AccountRepository {

    private final SpringDataAccountRepository springDataAccountRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = accountMapper.toJpaEntity(account);
        AccountJpaEntity saved = springDataAccountRepository.save(entity);
        return accountMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return springDataAccountRepository.findById(id)
                .map(accountMapper::toDomain);
    }

    @Override
    public List<Account> findByUserId(Long userId) {
        return springDataAccountRepository.findByUserId(userId)
                .stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Account> findByCbu(String cbu) {
        return springDataAccountRepository.findByCbu(cbu).map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByAlias(String alias) {
        return springDataAccountRepository.findByAlias(alias).map(accountMapper::toDomain);
    }
}