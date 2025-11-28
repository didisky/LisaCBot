package com.lisacbot.domain.strategy;

import com.lisacbot.domain.model.Signal;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple Moving Average (SMA) crossover strategy.
 * - BUY when price crosses above the moving average
 * - SELL when price crosses below the moving average
 */
public class SimpleMovingAverageStrategy implements TradingStrategy {
    private final Queue<Double> priceHistory = new LinkedList<>();
    private final int period;
    private Double lastAverage = null;

    public SimpleMovingAverageStrategy(int period) {
        this.period = period;
    }

    @Override
    public Signal analyze(double currentPrice) {
        priceHistory.add(currentPrice);

        if (priceHistory.size() > period) {
            priceHistory.poll();
        }

        if (priceHistory.size() < period) {
            return Signal.HOLD;
        }

        double average = priceHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(currentPrice);

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

    public int getPeriod() {
        return period;
    }

    public int getDataPoints() {
        return priceHistory.size();
    }
}
