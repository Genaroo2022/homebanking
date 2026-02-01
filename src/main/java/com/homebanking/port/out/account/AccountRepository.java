package com.homebanking.port.out.account;

import com.homebanking.domain.entity.Account;
import com.homebanking.domain.valueobject.common.Cbu;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    List<Account> findByUserId(UUID userId);
    Optional<Account> findByCbu(Cbu cbu);
    boolean existsByCbu(Cbu cbu);
}

