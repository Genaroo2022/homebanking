package com.homebanking.adapter.out.persistence.account;

import com.homebanking.domain.entity.Account;
import org.springframework.stereotype.Component;

@Component
class AccountMapper {

    public AccountJpaEntity toJpaEntity(Account account) {
        AccountJpaEntity entity = new AccountJpaEntity(
                account.getUserId(),
                account.getCbu().value(),
                account.getAlias().value(),
                account.getBalance().value(),
                account.getCreatedAt()
        );

        if (account.getId() != null) {
            entity.setId(account.getId());
        }

        return entity;
    }

    public Account toDomain(AccountJpaEntity entity) {
        return Account.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCbu(),
                entity.getAlias(),
                entity.getBalance(),
                entity.getCreatedAt()
        );
    }
}


