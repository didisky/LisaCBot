package com.lisacbot.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyConfiguration {

    @Value("${bot.strategy.type}")
    private String strategyType;

    @Value("${bot.strategy.sma.period}")
    private int smaPeriod;

    /**
     * Creates a TradingStrategy bean based on the configured strategy type.
     * Supported types:
     * - "sma": Simple Moving Average strategy
     *
     * @return configured TradingStrategy instance
     * @throws IllegalArgumentException if strategy type is unknown
     */
    @Bean
    public TradingStrategy tradingStrategy() {
        return switch (strategyType.toLowerCase()) {
            case "sma" -> new SimpleMovingAverageStrategy(smaPeriod);
            default -> throw new IllegalArgumentException(
                    "Unknown strategy type: " + strategyType +
                    ". Supported types: sma"
            );
        };
    }
}
