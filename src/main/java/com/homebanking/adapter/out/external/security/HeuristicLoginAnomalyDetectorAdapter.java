package com.homebanking.adapter.out.external.security;

import com.homebanking.domain.event.LoginAttemptedEvent;
import com.homebanking.port.out.security.LoginAnomalyDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prototype anomaly detector.
 * Keeps in-memory signals and emits high-risk alerts for operational response.
 */
@Component
@Slf4j
public class HeuristicLoginAnomalyDetectorAdapter implements LoginAnomalyDetector {

    private final Set<String> suspiciousIps = ConcurrentHashMap.newKeySet();

    @Override
    public void analyze(LoginAttemptedEvent event) {
        if (event.blocked()) {
            suspiciousIps.add(event.ipAddress());
            log.warn("SECURITY_ALERT login_blocked email={} ip={} at={}",
                    event.email().value(),
                    event.ipAddress(),
                    event.occurredAt());
            return;
        }

        if (!event.successful() && suspiciousIps.contains(event.ipAddress())) {
            log.warn("SECURITY_ALERT repeated_failed_login email={} ip={} at={}",
                    event.email().value(),
                    event.ipAddress(),
                    event.occurredAt());
            return;
        }

        if (event.successful() && suspiciousIps.remove(event.ipAddress())) {
            log.info("SECURITY_EVENT suspicious_ip_cleared email={} ip={} at={}",
                    event.email().value(),
                    event.ipAddress(),
                    event.occurredAt());
        }
    }
}

