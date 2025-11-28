package com.lisacbot.domain.strategy;

import com.lisacbot.domain.model.Signal;

/**
 * Interface for trading strategies.
 * Implementations contain the business logic for analyzing prices and generating trading signals.
 */
public interface TradingStrategy {
    /**
     * Analyzes the current price and generates a trading signal.
     *
     * @param currentPrice current cryptocurrency price
     * @return trading signal (BUY, SELL, or HOLD)
     */
    Signal analyze(double currentPrice);
}
