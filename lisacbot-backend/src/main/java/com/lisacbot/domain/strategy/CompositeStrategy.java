package com.lisacbot.domain.strategy;

import com.lisacbot.domain.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite strategy that combines multiple trading strategies using weighted voting.
 * Each strategy provides a signal (BUY/SELL/HOLD) with an associated weight.
 * The final decision is made by aggregating weighted signals.
 */
public class CompositeStrategy implements TradingStrategy {
    private static final Logger log = LoggerFactory.getLogger(CompositeStrategy.class);

    private final List<WeightedStrategy> strategies;
    private final double buyThreshold;
    private final double sellThreshold;

    /**
     * Represents a strategy with its weight.
     */
    public record WeightedStrategy(TradingStrategy strategy, double weight, String name) {
    }

    public CompositeStrategy(List<WeightedStrategy> strategies, double buyThreshold, double sellThreshold) {
        this.strategies = new ArrayList<>(strategies);
        this.buyThreshold = buyThreshold;
        this.sellThreshold = sellThreshold;

        // Validate weights sum to approximately 100
        double totalWeight = strategies.stream().mapToDouble(WeightedStrategy::weight).sum();
        if (Math.abs(totalWeight - 100.0) > 0.01) {
            log.warn("Strategy weights sum to {} instead of 100", totalWeight);
        }
    }

    @Override
    public Signal analyze(double currentPrice) {
        double weightedScore = 0.0;

        // Collect signals from all strategies and calculate weighted score
        for (WeightedStrategy ws : strategies) {
            Signal signal = ws.strategy().analyze(currentPrice);
            double contribution = signalToScore(signal) * (ws.weight() / 100.0);
            weightedScore += contribution;

            log.debug("{} ({}%): {} → contribution: {}",
                    ws.name(),
                    String.format("%.0f", ws.weight()),
                    signal,
                    String.format("%.2f", contribution));
        }

        // Determine final signal based on weighted score
        Signal finalSignal;
        if (weightedScore >= buyThreshold) {
            finalSignal = Signal.BUY;
        } else if (weightedScore <= sellThreshold) {
            finalSignal = Signal.SELL;
        } else {
            finalSignal = Signal.HOLD;
        }

        log.info("Composite strategy - Weighted score: {}, Decision: {}",
                String.format("%.2f", weightedScore),
                finalSignal);

        return finalSignal;
    }

    /**
     * Converts a signal to a numeric score for weighted voting.
     * BUY = +1, HOLD = 0, SELL = -1
     */
    private double signalToScore(Signal signal) {
        return switch (signal) {
            case BUY -> 1.0;
            case SELL -> -1.0;
            case HOLD -> 0.0;
        };
    }
}
