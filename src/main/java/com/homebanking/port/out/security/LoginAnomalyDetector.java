package com.homebanking.port.out.security;

import com.homebanking.domain.event.LoginAttemptedEvent;

/**
 * Output port for anomaly detection pipelines.
 */
public interface LoginAnomalyDetector {

    /**
     * Analyze a login attempt and route security signals.
     */
    void analyze(LoginAttemptedEvent event);
}

