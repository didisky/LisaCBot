package com.lisacbot.infrastructure.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for bot configuration operations.
 */
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class ConfigurationController {

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
}
