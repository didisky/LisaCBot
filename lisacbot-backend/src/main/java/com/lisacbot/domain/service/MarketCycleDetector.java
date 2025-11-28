package com.lisacbot.domain.service;

import com.lisacbot.domain.model.MarketCycle;
import com.lisacbot.domain.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for detecting market cycles based on price history analysis.
 * Uses multiple technical indicators to identify the current market phase.
 */
@Service
public class MarketCycleDetector {
    private static final Logger log = LoggerFactory.getLogger(MarketCycleDetector.class);

    private final int analysisWindowDays;
    private final double crashThreshold;
    private final double bullMarketThreshold;
    private final double volatilityLowThreshold;
    private final double volatilityHighThreshold;

    public MarketCycleDetector(
            @Value("${bot.cycle.analysis.window.days}") int analysisWindowDays,
            @Value("${bot.cycle.crash.threshold}") double crashThreshold,
            @Value("${bot.cycle.bull.threshold}") double bullMarketThreshold,
            @Value("${bot.cycle.volatility.low}") double volatilityLowThreshold,
            @Value("${bot.cycle.volatility.high}") double volatilityHighThreshold
    ) {
        this.analysisWindowDays = analysisWindowDays;
        this.crashThreshold = crashThreshold;
        this.bullMarketThreshold = bullMarketThreshold;
        this.volatilityLowThreshold = volatilityLowThreshold;
        this.volatilityHighThreshold = volatilityHighThreshold;
    }

    /**
     * Detects the current market cycle based on historical price data.
     *
     * @param historicalPrices list of historical prices (should contain at least analysisWindowDays data points)
     * @return the detected market cycle
     */
    public MarketCycle detectCycle(List<Price> historicalPrices) {
        if (historicalPrices.isEmpty()) {
            log.warn("No historical data available for cycle detection");
            return MarketCycle.UNKNOWN;
        }

        // Extract price values
        double[] prices = historicalPrices.stream()
                .mapToDouble(Price::value)
                .toArray();

        if (prices.length < analysisWindowDays) {
            log.warn("Insufficient data for reliable cycle detection (need at least {} days, got {})",
                    analysisWindowDays, prices.length);
            return MarketCycle.UNKNOWN;
        }

        // Calculate indicators
        double momentum = calculateMomentum(prices);
        double volatility = calculateVolatility(prices);
        double trend = calculateTrend(prices);
        double recentChange = calculateRecentChange(prices);

        log.info("Market indicators - Momentum: {}, Volatility: {}, Trend: {}, Recent change: {}%",
                String.format("%.2f", momentum),
                String.format("%.2f%%", volatility * 100),
                String.format("%.2f", trend),
                String.format("%.2f", recentChange));

        // Detect cycle based on indicators
        MarketCycle cycle = determineCycle(momentum, volatility, trend, recentChange);
        log.info("Detected market cycle: {}", cycle);

        return cycle;
    }

    /**
     * Calculates momentum as the rate of change over the analysis window.
     */
    private double calculateMomentum(double[] prices) {
        int windowSize = Math.min(analysisWindowDays, prices.length);
        double oldPrice = prices[prices.length - windowSize];
        double currentPrice = prices[prices.length - 1];
        return ((currentPrice - oldPrice) / oldPrice) * 100.0;
    }

    /**
     * Calculates volatility as the standard deviation of price changes.
     */
    private double calculateVolatility(double[] prices) {
        double[] returns = new double[prices.length - 1];
        for (int i = 0; i < returns.length; i++) {
            returns[i] = (prices[i + 1] - prices[i]) / prices[i];
        }

        double mean = 0.0;
        for (double ret : returns) {
            mean += ret;
        }
        mean /= returns.length;

        double variance = 0.0;
        for (double ret : returns) {
            variance += Math.pow(ret - mean, 2);
        }
        variance /= returns.length;

        return Math.sqrt(variance);
    }

    /**
     * Calculates trend using simple moving average slope.
     * Positive values indicate uptrend, negative indicate downtrend.
     */
    private double calculateTrend(double[] prices) {
        int shortPeriod = Math.min(7, prices.length);
        int longPeriod = Math.min(30, prices.length);

        double shortSMA = calculateSMA(prices, shortPeriod);
        double longSMA = calculateSMA(prices, longPeriod);

        return ((shortSMA - longSMA) / longSMA) * 100.0;
    }

    /**
     * Calculates recent change over the last few days to detect rapid movements.
     */
    private double calculateRecentChange(double[] prices) {
        int recentDays = Math.min(3, prices.length);
        double oldPrice = prices[prices.length - recentDays];
        double currentPrice = prices[prices.length - 1];
        return ((currentPrice - oldPrice) / oldPrice) * 100.0;
    }

    /**
     * Calculates Simple Moving Average for the given period.
     */
    private double calculateSMA(double[] prices, int period) {
        double sum = 0.0;
        int start = Math.max(0, prices.length - period);
        for (int i = start; i < prices.length; i++) {
            sum += prices[i];
        }
        return sum / (prices.length - start);
    }

    /**
     * Determines the market cycle based on calculated indicators.
     */
    private MarketCycle determineCycle(double momentum, double volatility, double trend, double recentChange) {
        // CRASH: Rapid decline with very high volatility
        if (recentChange < crashThreshold && volatility > volatilityHighThreshold) {
            return MarketCycle.CRASH;
        }

        // BULL_MARKET: Strong sustained uptrend with high momentum
        if (momentum > bullMarketThreshold && trend > 5.0 && volatility < volatilityHighThreshold) {
            return MarketCycle.BULL_MARKET;
        }

        // MARKUP: Uptrend beginning, positive momentum and trend
        if (momentum > 5.0 && trend > 2.0) {
            return MarketCycle.MARKUP;
        }

        // DECLINE: Downtrend with negative momentum
        if (momentum < -5.0 && trend < -2.0) {
            return MarketCycle.DECLINE;
        }

        // ACCUMULATION: Low volatility, sideways movement
        if (volatility < volatilityLowThreshold && Math.abs(momentum) < 5.0) {
            return MarketCycle.ACCUMULATION;
        }

        // Default to ACCUMULATION if no clear trend
        return MarketCycle.ACCUMULATION;
    }
}
