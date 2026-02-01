package com.homebanking.port.out;

import com.homebanking.domain.model.LoginAttempt;
import java.util.List;

public interface LoginAttemptRepository {
    void save(LoginAttempt attempt);
    List<LoginAttempt> findRecentFailedAttempts(String username);
    void resetFailedAttempts(String username);
}
