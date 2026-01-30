package com.homebanking.port.out;

public interface EventPublisher {
    void publish(Object event);
}
