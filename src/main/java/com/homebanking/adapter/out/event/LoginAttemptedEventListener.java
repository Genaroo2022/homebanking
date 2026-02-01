package com.homebanking.adapter.out.event;

import com.homebanking.domain.event.LoginAttemptedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoginAttemptedEventListener {

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

        // TODO: En una fase posterior, enviar a un pipeline de deteccion de anomalias.
    }
}
