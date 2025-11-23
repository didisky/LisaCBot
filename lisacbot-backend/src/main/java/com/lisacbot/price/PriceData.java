package com.lisacbot.price;

/**
 * Represents a single price data point with timestamp.
 */
public record PriceData(long timestamp, double price) {
}
