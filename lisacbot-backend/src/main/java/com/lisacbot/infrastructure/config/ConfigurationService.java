package com.lisacbot.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for managing runtime configuration parameters.
 * Allows dynamic updates to strategy parameters without restarting the application.
 */
@Service
public class ConfigurationService {

    // Default values from application.properties
    @Value("${bot.strategy.sma.period}")
    private int defaultSmaPeriod;

    @Value("${bot.strategy.ema.period}")
    private int defaultEmaPeriod;

    @Value("${bot.strategy.rsi.period}")
    private int defaultRsiPeriod;

    @Value("${bot.strategy.rsi.oversold}")
    private int defaultRsiOversold;

    @Value("${bot.strategy.rsi.overbought}")
    private int defaultRsiOverbought;

    @Value("${bot.strategy.macd.fast.period}")
    private int defaultMacdFastPeriod;

    @Value("${bot.strategy.macd.slow.period}")
    private int defaultMacdSlowPeriod;

    @Value("${bot.strategy.macd.signal.period}")
    private int defaultMacdSignalPeriod;

    @Value("${bot.strategy.composite.buy.threshold:0.5}")
    private double defaultCompositeBuyThreshold;

    @Value("${bot.strategy.composite.sell.threshold:-0.5}")
    private double defaultCompositeSellThreshold;

    // Runtime configuration values (can be updated)
    private int smaPeriod;
    private int emaPeriod;
    private int rsiPeriod;
    private int rsiOversold;
    private int rsiOverbought;
    private int macdFastPeriod;
    private int macdSlowPeriod;
    private int macdSignalPeriod;
    private double compositeBuyThreshold;
    private double compositeSellThreshold;

    @PostConstruct
    public void initialize() {
        // Initialize runtime values with defaults from application.properties
        this.smaPeriod = defaultSmaPeriod;
        this.emaPeriod = defaultEmaPeriod;
        this.rsiPeriod = defaultRsiPeriod;
        this.rsiOversold = defaultRsiOversold;
        this.rsiOverbought = defaultRsiOverbought;
        this.macdFastPeriod = defaultMacdFastPeriod;
        this.macdSlowPeriod = defaultMacdSlowPeriod;
        this.macdSignalPeriod = defaultMacdSignalPeriod;
        this.compositeBuyThreshold = defaultCompositeBuyThreshold;
        this.compositeSellThreshold = defaultCompositeSellThreshold;
    }

    // Getters
    public int getSmaPeriod() {
        return smaPeriod;
    }

    public int getEmaPeriod() {
        return emaPeriod;
    }

    public int getRsiPeriod() {
        return rsiPeriod;
    }

    public int getRsiOversold() {
        return rsiOversold;
    }

    public int getRsiOverbought() {
        return rsiOverbought;
    }

    public int getMacdFastPeriod() {
        return macdFastPeriod;
    }

    public int getMacdSlowPeriod() {
        return macdSlowPeriod;
    }

    public int getMacdSignalPeriod() {
        return macdSignalPeriod;
    }

    public double getCompositeBuyThreshold() {
        return compositeBuyThreshold;
    }

    public double getCompositeSellThreshold() {
        return compositeSellThreshold;
    }

    // Setters for runtime updates
    public synchronized void updateConfiguration(
            int smaPeriod,
            int emaPeriod,
            int rsiPeriod,
            int rsiOversold,
            int rsiOverbought,
            int macdFastPeriod,
            int macdSlowPeriod,
            int macdSignalPeriod,
            double compositeBuyThreshold,
            double compositeSellThreshold
    ) {
        this.smaPeriod = smaPeriod;
        this.emaPeriod = emaPeriod;
        this.rsiPeriod = rsiPeriod;
        this.rsiOversold = rsiOversold;
        this.rsiOverbought = rsiOverbought;
        this.macdFastPeriod = macdFastPeriod;
        this.macdSlowPeriod = macdSlowPeriod;
        this.macdSignalPeriod = macdSignalPeriod;
        this.compositeBuyThreshold = compositeBuyThreshold;
        this.compositeSellThreshold = compositeSellThreshold;
    }
}
