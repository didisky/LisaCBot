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
public class ConfigurationController {

    private final TradingService tradingService;
    private final com.lisacbot.infrastructure.config.BotScheduler botScheduler;
    private final com.lisacbot.infrastructure.config.ConfigurationService configurationService;

    // Composite strategy configuration (still from properties as these define the strategy structure)
    @Value("${bot.strategy.composite.strategies:}")
    private String compositeStrategies;

    @Value("${bot.strategy.composite.weights:}")
    private String compositeWeights;

    public ConfigurationController(
            TradingService tradingService,
            com.lisacbot.infrastructure.config.BotScheduler botScheduler,
            com.lisacbot.infrastructure.config.ConfigurationService configurationService
    ) {
        this.tradingService = tradingService;
        this.botScheduler = botScheduler;
        this.configurationService = configurationService;
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
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("pollIntervalSeconds", botScheduler.getCurrentPollInterval());
        config.put("smaPeriod", configurationService.getSmaPeriod());
        config.put("emaPeriod", configurationService.getEmaPeriod());
        config.put("rsiPeriod", configurationService.getRsiPeriod());
        config.put("rsiOversold", configurationService.getRsiOversold());
        config.put("rsiOverbought", configurationService.getRsiOverbought());
        config.put("macdFastPeriod", configurationService.getMacdFastPeriod());
        config.put("macdSlowPeriod", configurationService.getMacdSlowPeriod());
        config.put("macdSignalPeriod", configurationService.getMacdSignalPeriod());
        config.put("compositeBuyThreshold", configurationService.getCompositeBuyThreshold());
        config.put("compositeSellThreshold", configurationService.getCompositeSellThreshold());

        // Add current strategy information
        config.put("strategyName", tradingService.getStrategyName());
        config.put("strategyParameters", tradingService.getStrategyParameters());

        return ResponseEntity.ok(config);
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

    /**
     * Updates the bot polling interval dynamically.
     */
    @PostMapping("/poll-interval")
    public ResponseEntity<Map<String, Object>> updatePollInterval(@RequestBody Map<String, Integer> request) {
        Integer intervalSeconds = request.get("pollIntervalSeconds");

        if (intervalSeconds == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Poll interval is required"
            ));
        }

        try {
            botScheduler.updatePollInterval(intervalSeconds);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Poll interval updated successfully to " + intervalSeconds + " seconds",
                    "pollIntervalSeconds", intervalSeconds
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Updates all strategy configuration parameters dynamically.
     */
    @PostMapping("/parameters")
    public ResponseEntity<Map<String, Object>> updateParameters(@RequestBody Map<String, Object> request) {
        try {
            int smaPeriod = ((Number) request.get("smaPeriod")).intValue();
            int emaPeriod = ((Number) request.get("emaPeriod")).intValue();
            int rsiPeriod = ((Number) request.get("rsiPeriod")).intValue();
            int rsiOversold = ((Number) request.get("rsiOversold")).intValue();
            int rsiOverbought = ((Number) request.get("rsiOverbought")).intValue();
            int macdFastPeriod = ((Number) request.get("macdFastPeriod")).intValue();
            int macdSlowPeriod = ((Number) request.get("macdSlowPeriod")).intValue();
            int macdSignalPeriod = ((Number) request.get("macdSignalPeriod")).intValue();
            double compositeBuyThreshold = ((Number) request.get("compositeBuyThreshold")).doubleValue();
            double compositeSellThreshold = ((Number) request.get("compositeSellThreshold")).doubleValue();

            configurationService.updateConfiguration(
                    smaPeriod, emaPeriod, rsiPeriod, rsiOversold, rsiOverbought,
                    macdFastPeriod, macdSlowPeriod, macdSignalPeriod,
                    compositeBuyThreshold, compositeSellThreshold
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Configuration parameters updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to update parameters: " + e.getMessage()
            ));
        }
    }

    private TradingStrategy createStrategyByType(String strategyType) {
        return switch (strategyType) {
            case "sma" -> new SimpleMovingAverageStrategy(configurationService.getSmaPeriod());
            case "ema-rsi" -> new EmaRsiStrategy(
                    configurationService.getEmaPeriod(),
                    configurationService.getRsiPeriod(),
                    configurationService.getRsiOversold(),
                    configurationService.getRsiOverbought()
            );
            case "macd" -> new MacdStrategy(
                    configurationService.getMacdFastPeriod(),
                    configurationService.getMacdSlowPeriod(),
                    configurationService.getMacdSignalPeriod()
            );
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

        return new CompositeStrategy(
                weightedStrategies,
                configurationService.getCompositeBuyThreshold(),
                configurationService.getCompositeSellThreshold()
        );
    }

    private TradingStrategy createStrategyByName(String name) {
        return switch (name.toLowerCase()) {
            case "sma" -> new SimpleMovingAverageStrategy(configurationService.getSmaPeriod());
            case "ema-rsi" -> new EmaRsiStrategy(
                    configurationService.getEmaPeriod(),
                    configurationService.getRsiPeriod(),
                    configurationService.getRsiOversold(),
                    configurationService.getRsiOverbought()
            );
            case "macd" -> new MacdStrategy(
                    configurationService.getMacdFastPeriod(),
                    configurationService.getMacdSlowPeriod(),
                    configurationService.getMacdSignalPeriod()
            );
            default -> throw new IllegalArgumentException(
                    "Unknown strategy name in composite: " + name +
                    ". Supported: sma, ema-rsi, macd"
            );
        };
    }
}
