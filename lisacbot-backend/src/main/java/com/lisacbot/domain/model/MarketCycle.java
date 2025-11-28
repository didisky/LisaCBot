package com.lisacbot.domain.model;

/**
 * Represents the different phases of a market cycle.
 * Market cycles help identify the overall market sentiment and adjust trading strategies accordingly.
 */
public enum MarketCycle {
    /**
     * Accumulation phase: Bottom phase with consolidation, low volatility, sideways movement.
     * Smart money accumulates positions while sentiment is negative.
     */
    ACCUMULATION,

    /**
     * Mark-up phase: Uptrend begins, increasing momentum, higher highs and higher lows.
     * Trend becomes clear and more investors join.
     */
    MARKUP,

    /**
     * Bull market phase: Strong sustained uptrend, high momentum, widespread optimism.
     * Market reaches peak enthusiasm.
     */
    BULL_MARKET,

    /**
     * Decline phase: Downtrend with lower highs and lower lows, decreasing momentum.
     * Sentiment turns negative, distribution begins.
     */
    DECLINE,

    /**
     * Crash phase: Rapid decline, very high volatility, panic selling.
     * Sharp price drops with high volume and fear.
     */
    CRASH,

    /**
     * Unknown: Not enough data to determine the market cycle reliably.
     */
    UNKNOWN
}
