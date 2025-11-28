package com.lisacbot.domain.strategy;

import com.lisacbot.domain.model.Signal;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Combined EMA + RSI trading strategy.
 *
 * Uses two indicators:
 * - EMA (Exponential Moving Average): Trend following indicator
 * - RSI (Relative Strength Index): Momentum indicator
 *
 * Trading rules:
 * - BUY: Price crosses above EMA AND RSI < 30 (oversold)
 * - SELL: Price crosses below EMA AND RSI > 70 (overbought)
 * - HOLD: Otherwise
 */
public class EmaRsiStrategy implements TradingStrategy {
    private final Queue<Double> priceHistory = new LinkedList<>();
    private final int emaPeriod;
    private final int rsiPeriod;
    private final int rsiOversold;
    private final int rsiOverbought;

    private Double ema = null;
    private final double smoothingFactor;

    public EmaRsiStrategy(int emaPeriod, int rsiPeriod, int rsiOversold, int rsiOverbought) {
        this.emaPeriod = emaPeriod;
        this.rsiPeriod = rsiPeriod;
        this.rsiOversold = rsiOversold;
        this.rsiOverbought = rsiOverbought;
        this.smoothingFactor = 2.0 / (emaPeriod + 1);
    }

    @Override
    public Signal analyze(double currentPrice) {
        priceHistory.add(currentPrice);

        // Keep only the data we need (max of EMA and RSI periods)
        int maxPeriod = Math.max(emaPeriod, rsiPeriod + 1);
        while (priceHistory.size() > maxPeriod + 1) {
            priceHistory.poll();
        }

        // Wait until we have enough data
        if (priceHistory.size() < Math.max(emaPeriod, rsiPeriod + 1)) {
            return Signal.HOLD;
        }

        // Calculate EMA
        if (ema == null) {
            // Initialize EMA with SMA
            ema = priceHistory.stream()
                    .limit(emaPeriod)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(currentPrice);
        } else {
            // Update EMA: EMA = Price * smoothing + EMA_prev * (1 - smoothing)
            ema = currentPrice * smoothingFactor + ema * (1 - smoothingFactor);
        }

        // Calculate RSI
        double rsi = calculateRSI();

        // Generate signal based on EMA trend and RSI levels
        boolean priceAboveEma = currentPrice > ema;
        boolean priceBelowEma = currentPrice < ema;

        if (priceAboveEma && rsi < rsiOversold) {
            return Signal.BUY;  // Uptrend + oversold = buy opportunity
        } else if (priceBelowEma && rsi > rsiOverbought) {
            return Signal.SELL; // Downtrend + overbought = sell opportunity
        }

        return Signal.HOLD;
    }

    /**
     * Calculates the Relative Strength Index (RSI).
     * RSI = 100 - (100 / (1 + RS))
     * where RS = Average Gain / Average Loss over the period
     */
    private double calculateRSI() {
        if (priceHistory.size() < rsiPeriod + 1) {
            return 50.0; // Neutral RSI if not enough data
        }

        Double[] prices = priceHistory.toArray(new Double[0]);
        double avgGain = 0.0;
        double avgLoss = 0.0;

        // Calculate initial average gain and loss
        for (int i = prices.length - rsiPeriod - 1; i < prices.length - 1; i++) {
            double change = prices[i + 1] - prices[i];
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss += Math.abs(change);
            }
        }

        avgGain /= rsiPeriod;
        avgLoss /= rsiPeriod;

        if (avgLoss == 0) {
            return 100.0; // No losses = maximum RSI
        }

        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    public int getEmaPeriod() {
        return emaPeriod;
    }

    public int getRsiPeriod() {
        return rsiPeriod;
    }

    public Double getEma() {
        return ema;
    }

    public int getDataPoints() {
        return priceHistory.size();
    }
}
