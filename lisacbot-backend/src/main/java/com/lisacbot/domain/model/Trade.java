package com.lisacbot.domain.model;

import java.time.LocalDateTime;

/**
 * Represents a trade execution.
 */
public record Trade(
        Signal signal,
        double price,
        double amount,
        LocalDateTime timestamp
) {
    public Trade(Signal signal, double price, double amount) {
        this(signal, price, amount, LocalDateTime.now());
    }
}
