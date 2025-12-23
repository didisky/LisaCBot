package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.service.TradingService;
import com.lisacbot.domain.strategy.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST controller for bot configuration operations.
 */
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class ConfigurationController {

    private final TradingService tradingService;

    // Bot configuration
    @Value("${bot.poll.interval.seconds}")
    private int pollIntervalSeconds;

    // Strategy parameters from configuration
    @Value("${bot.strategy.sma.period}")
    private int smaPeriod;

    @Value("${bot.strategy.ema.period}")
    private int emaPeriod;

    @Value("${bot.strategy.rsi.period}")
    private int rsiPeriod;

    @Value("${bot.strategy.rsi.oversold}")
    private int rsiOversold;

    @Value("${bot.strategy.rsi.overbought}")
    private int rsiOverbought;

    @Value("${bot.strategy.macd.fast.period}")
    private int macdFastPeriod;

    @Value("${bot.strategy.macd.slow.period}")
    private int macdSlowPeriod;

    @Value("${bot.strategy.macd.signal.period}")
    private int macdSignalPeriod;

    @Value("${bot.strategy.composite.strategies:}")
    private String compositeStrategies;

    @Value("${bot.strategy.composite.weights:}")
    private String compositeWeights;

    @Value("${bot.strategy.composite.buy.threshold:0.5}")
    private double compositeBuyThreshold;

    @Value("${bot.strategy.composite.sell.threshold:-0.5}")
    private double compositeSellThreshold;

    public ConfigurationController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Returns the list of available trading strategies.
     */
    @GetMapping("/strategies")
    public ResponseEntity<List<Map<String, String>>> getAvailableStrategies() {
        List<Map<String, String>> strategies = List.of(
                Map.of(
                        "value", "SMA",
                        "label", "Simple Moving Average (SMA)",
                        "description", "Uses moving average crossovers to generate buy/sell signals"
                ),
                Map.of(
                        "value", "EMA-RSI",
                        "label", "EMA + RSI Strategy",
                        "description", "Combines Exponential Moving Average with Relative Strength Index"
                ),
                Map.of(
                        "value", "MACD",
                        "label", "MACD Strategy",
                        "description", "Moving Average Convergence Divergence momentum indicator"
                ),
                Map.of(
                        "value", "COMPOSITE",
                        "label", "Composite Strategy",
                        "description", "Combines multiple strategies with weighted voting"
                )
        );

        return ResponseEntity.ok(strategies);
    }

    /**
     * Returns the current bot configuration.
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentConfiguration() {
        return ResponseEntity.ok(Map.of(
                "pollIntervalSeconds", pollIntervalSeconds,
                "smaPeriod", smaPeriod,
                "emaPeriod", emaPeriod,
                "rsiPeriod", rsiPeriod,
                "macdFastPeriod", macdFastPeriod,
                "macdSlowPeriod", macdSlowPeriod,
                "macdSignalPeriod", macdSignalPeriod
        ));
    }

    /**
     * Updates the active trading strategy.
     */
    @PostMapping("/strategy")
    public ResponseEntity<Map<String, Object>> updateStrategy(@RequestBody Map<String, String> request) {
        String strategyType = request.get("type");

        if (strategyType == null || strategyType.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Strategy type is required"
            ));
        }

        try {
            TradingStrategy newStrategy = createStrategyByType(strategyType.toLowerCase());
            tradingService.updateStrategy(newStrategy, strategyType.toUpperCase());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Strategy updated successfully to " + strategyType.toUpperCase(),
                    "strategy", strategyType.toUpperCase()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    private TradingStrategy createStrategyByType(String strategyType) {
        return switch (strategyType) {
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
        String[] strategyNames = compositeStrategies.split(",");
        String[] weightStrings = compositeWeights.split(",");

        if (strategyNames.length != weightStrings.length) {
            throw new IllegalArgumentException(
                    "Number of strategies (" + strategyNames.length +
                    ") must match number of weights (" + weightStrings.length + ")"
            );
        }

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
