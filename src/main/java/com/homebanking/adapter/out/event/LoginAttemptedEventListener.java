package com.homebanking.adapter.out.event;

import com.homebanking.domain.event.LoginAttemptedEvent;
import com.homebanking.port.out.security.LoginAnomalyDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptedEventListener {

    private final LoginAnomalyDetector loginAnomalyDetector;

    @EventListener
    public void onLoginAttempted(LoginAttemptedEvent event) {
        log.info(
                "LoginAttemptedEvent received: email={}, ip={}, successful={}, blocked={}, occurredAt={}",
                event.email().value(),
                event.ipAddress(),
                event.successful(),
                event.blocked(),
                event.occurredAt()
        );
        loginAnomalyDetector.analyze(event);
    }
}


