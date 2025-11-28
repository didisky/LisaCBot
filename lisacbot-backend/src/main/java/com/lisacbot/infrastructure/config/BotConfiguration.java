package com.lisacbot.infrastructure.config;

import com.lisacbot.domain.strategy.EmaRsiStrategy;
import com.lisacbot.domain.strategy.SimpleMovingAverageStrategy;
import com.lisacbot.domain.strategy.TradingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the trading bot.
 */
@Configuration
public class BotConfiguration {

    @Value("${bot.strategy.type}")
    private String strategyType;

    // SMA configuration
    @Value("${bot.strategy.sma.period}")
    private int smaPeriod;

    // EMA+RSI configuration
    @Value("${bot.strategy.ema.period:20}")
    private int emaPeriod;

    @Value("${bot.strategy.rsi.period:14}")
    private int rsiPeriod;

    @Value("${bot.strategy.rsi.oversold:30}")
    private int rsiOversold;

    @Value("${bot.strategy.rsi.overbought:70}")
    private int rsiOverbought;

    @Bean
    public TradingStrategy tradingStrategy() {
        return switch (strategyType.toLowerCase()) {
            case "sma" -> new SimpleMovingAverageStrategy(smaPeriod);
            case "ema-rsi" -> new EmaRsiStrategy(emaPeriod, rsiPeriod, rsiOversold, rsiOverbought);
            default -> throw new IllegalArgumentException(
                    "Unknown strategy type: " + strategyType +
                    ". Supported types: sma, ema-rsi"
            );
        };
    }
}
