package com.lisacbot;

/**
 * Represents a single price data point with timestamp.
 */
public record PriceData(long timestamp, double price) {
}
