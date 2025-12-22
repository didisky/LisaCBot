package com.lisacbot.domain.model;

/**
 * Represents the current status of the trading bot.
 */
public record BotStatus(
        boolean running,
        double balance,
        double holdings,
        double lastPrice,
        double totalValue,
        MarketCycle marketCycle,
        String strategyName
) {
}
