package com.lisacbot.domain.strategy;

import com.lisacbot.domain.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MACD (Moving Average Convergence Divergence) trading strategy.
 *
 * MACD is a trend-following momentum indicator that shows the relationship
 * between two moving averages of a security's price.
 *
 * Components:
 * - MACD Line: (12-period EMA - 26-period EMA)
 * - Signal Line: 9-period EMA of MACD Line
 *
 * Trading signals:
 * - BUY: MACD line crosses above Signal line (bullish crossover)
 * - SELL: MACD line crosses below Signal line (bearish crossover)
 * - HOLD: No crossover detected
 */
public class MacdStrategy implements TradingStrategy {
    private static final Logger log = LoggerFactory.getLogger(MacdStrategy.class);

    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;

    private double fastEma;
    private double slowEma;
    private double signalEma;
    private double previousMacd;
    private double previousSignal;
    private boolean initialized;

    public MacdStrategy(int fastPeriod, int slowPeriod, int signalPeriod) {
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
        this.fastEma = 0.0;
        this.slowEma = 0.0;
        this.signalEma = 0.0;
        this.previousMacd = 0.0;
        this.previousSignal = 0.0;
        this.initialized = false;
    }

    @Override
    public Signal analyze(double currentPrice) {
        // Initialize EMAs with first price
        if (!initialized) {
            fastEma = currentPrice;
            slowEma = currentPrice;
            signalEma = 0.0;
            initialized = true;
            return Signal.HOLD;
        }

        // Calculate smoothing factors
        double fastSmoothing = 2.0 / (fastPeriod + 1);
        double slowSmoothing = 2.0 / (slowPeriod + 1);
        double signalSmoothing = 2.0 / (signalPeriod + 1);

        // Update fast and slow EMAs
        fastEma = currentPrice * fastSmoothing + fastEma * (1 - fastSmoothing);
        slowEma = currentPrice * slowSmoothing + slowEma * (1 - slowSmoothing);

        // Calculate MACD line (difference between fast and slow EMAs)
        double macdLine = fastEma - slowEma;

        // Update signal line (EMA of MACD line)
        if (signalEma == 0.0) {
            signalEma = macdLine;
        } else {
            signalEma = macdLine * signalSmoothing + signalEma * (1 - signalSmoothing);
        }

        // Detect crossovers
        Signal signal = detectCrossover(macdLine, signalEma);

        // Store current values for next iteration
        previousMacd = macdLine;
        previousSignal = signalEma;

        log.debug("MACD: {}, Signal: {}, Histogram: {} → {}",
                String.format("%.2f", macdLine),
                String.format("%.2f", signalEma),
                String.format("%.2f", macdLine - signalEma),
                signal);

        return signal;
    }

    /**
     * Detects crossovers between MACD line and Signal line.
     */
    private Signal detectCrossover(double macdLine, double signalLine) {
        // Bullish crossover: MACD crosses above Signal
        if (previousMacd <= previousSignal && macdLine > signalLine) {
            log.info("MACD: Bullish crossover detected");
            return Signal.BUY;
        }

        // Bearish crossover: MACD crosses below Signal
        if (previousMacd >= previousSignal && macdLine < signalLine) {
            log.info("MACD: Bearish crossover detected");
            return Signal.SELL;
        }

        // No crossover
        return Signal.HOLD;
    }
}
