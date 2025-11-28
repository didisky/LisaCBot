package com.lisacbot.infrastructure.config;

import com.lisacbot.domain.strategy.CompositeStrategy;
import com.lisacbot.domain.strategy.EmaRsiStrategy;
import com.lisacbot.domain.strategy.MacdStrategy;
import com.lisacbot.domain.strategy.SimpleMovingAverageStrategy;
import com.lisacbot.domain.strategy.TradingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

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
    @Value("${bot.strategy.ema.period}")
    private int emaPeriod;

    @Value("${bot.strategy.rsi.period}")
    private int rsiPeriod;

    @Value("${bot.strategy.rsi.oversold}")
    private int rsiOversold;

    @Value("${bot.strategy.rsi.overbought}")
    private int rsiOverbought;

    // MACD configuration
    @Value("${bot.strategy.macd.fast.period}")
    private int macdFastPeriod;

    @Value("${bot.strategy.macd.slow.period}")
    private int macdSlowPeriod;

    @Value("${bot.strategy.macd.signal.period}")
    private int macdSignalPeriod;

    // Composite strategy configuration
    @Value("${bot.strategy.composite.strategies:}")
    private String compositeStrategies;

    @Value("${bot.strategy.composite.weights:}")
    private String compositeWeights;

    @Value("${bot.strategy.composite.buy.threshold:0.5}")
    private double compositeBuyThreshold;

    @Value("${bot.strategy.composite.sell.threshold:-0.5}")
    private double compositeSellThreshold;

    @Bean
    public TradingStrategy tradingStrategy() {
        return switch (strategyType.toLowerCase()) {
            case "sma" -> new SimpleMovingAverageStrategy(smaPeriod);
            case "ema-rsi" -> new EmaRsiStrategy(emaPeriod, rsiPeriod, rsiOversold, rsiOverbought);
            case "macd" -> new MacdStrategy(macdFastPeriod, macdSlowPeriod, macdSignalPeriod);
            case "composite" -> createCompositeStrategy();
            default -> throw new IllegalArgumentException(
                    "Unknown strategy type: " + strategyType +
                    ". Supported types: sma, ema-rsi, macd, composite"
            );
        };
    }

    private TradingStrategy createCompositeStrategy() {
        // Parse strategy names and weights
        String[] strategyNames = compositeStrategies.split(",");
        String[] weightStrings = compositeWeights.split(",");

        if (strategyNames.length != weightStrings.length) {
            throw new IllegalArgumentException(
                    "Number of strategies (" + strategyNames.length +
                    ") must match number of weights (" + weightStrings.length + ")"
            );
        }

        // Create weighted strategies
        List<CompositeStrategy.WeightedStrategy> weightedStrategies = new ArrayList<>();
        for (int i = 0; i < strategyNames.length; i++) {
            String strategyName = strategyNames[i].trim();
            double weight = Double.parseDouble(weightStrings[i].trim());

            TradingStrategy strategy = createStrategyByName(strategyName);
            weightedStrategies.add(new CompositeStrategy.WeightedStrategy(strategy, weight, strategyName.toUpperCase()));
        }

        return new CompositeStrategy(weightedStrategies, compositeBuyThreshold, compositeSellThreshold);
    }

    private TradingStrategy createStrategyByName(String name) {
        return switch (name.toLowerCase()) {
            case "sma" -> new SimpleMovingAverageStrategy(smaPeriod);
            case "ema-rsi" -> new EmaRsiStrategy(emaPeriod, rsiPeriod, rsiOversold, rsiOverbought);
            case "macd" -> new MacdStrategy(macdFastPeriod, macdSlowPeriod, macdSignalPeriod);
            default -> throw new IllegalArgumentException(
                    "Unknown strategy name in composite: " + name +
                    ". Supported: sma, ema-rsi, macd"
            );
        };
    }
}
