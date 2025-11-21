package com.lisacbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple Moving Average (SMA) crossover strategy.
 * - BUY when price crosses above the moving average
 * - SELL when price crosses below the moving average
 */
@Component
public class SimpleMovingAverageStrategy implements TradingStrategy {
    private static final Logger log = LoggerFactory.getLogger(SimpleMovingAverageStrategy.class);

    private final Queue<Double> priceHistory = new LinkedList<>();

    @Value("${bot.strategy.sma.period}")
    private int period;

    private Double lastAverage = null;

    /**
     * Analyzes price using Simple Moving Average crossover.
     * Generates BUY when price crosses above SMA, SELL when below.
     *
     * @param currentPrice current BTC price
     * @return BUY, SELL, or HOLD signal
     */
    @Override
    public Signal analyze(double currentPrice) {
        priceHistory.add(currentPrice);

        if (priceHistory.size() > period) {
            priceHistory.poll();
        }

        if (priceHistory.size() < period) {
            log.info("Collecting data... ({}/{})", priceHistory.size(), period);
            return Signal.HOLD;
        }

        double average = priceHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(currentPrice);

        log.info("SMA({}): ${}", period, String.format("%.2f", average));

        Signal signal = Signal.HOLD;

        if (lastAverage != null) {
            if (currentPrice > average && currentPrice > lastAverage) {
                signal = Signal.BUY;
            } else if (currentPrice < average && currentPrice < lastAverage) {
                signal = Signal.SELL;
            }
        }

        lastAverage = average;
        return signal;
    }
}
