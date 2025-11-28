package com.lisacbot.domain.model;

import java.time.LocalDateTime;

/**
 * Represents a cryptocurrency price at a specific point in time.
 */
public record Price(
        double value,
        LocalDateTime timestamp
) {
    public Price(double value) {
        this(value, LocalDateTime.now());
    }
}
