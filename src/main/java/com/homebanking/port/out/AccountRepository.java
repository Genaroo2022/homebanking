package com.homebanking.port.out;

import com.homebanking.domain.entity.Account;
import java.util.Optional;
import java.util.List;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    List<Account> findByUserId(Long userId);
    Optional<Account> findByCbu(String cbu);
    Optional<Account> findByAlias(String alias);
}