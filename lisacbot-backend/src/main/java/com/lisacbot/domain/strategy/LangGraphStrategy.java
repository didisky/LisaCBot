package com.lisacbot.domain.strategy;

import com.lisacbot.domain.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * LangGraph-powered trading strategy.
 * Calls a Python script using ProcessBuilder for advanced technical analysis.
 */
public class LangGraphStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LangGraphStrategy.class);

    private final Queue<Double> priceHistory = new LinkedList<>();
    private final int maxHistorySize;
    private final String pythonScriptPath;
    private final ObjectMapper objectMapper;

    public LangGraphStrategy(int maxHistorySize, String pythonScriptPath) {
        this.maxHistorySize = maxHistorySize;
        this.pythonScriptPath = pythonScriptPath;
        this.objectMapper = new ObjectMapper();
        logger.info("LangGraphStrategy initialized with Python script: {}", pythonScriptPath);
    }

    public LangGraphStrategy(int maxHistorySize) {
        this(maxHistorySize, findDefaultPythonScript());
    }

    private static String findDefaultPythonScript() {
        // Try to find the Python script relative to project root
        Path projectRoot = Paths.get("").toAbsolutePath().getParent();
        Path scriptPath = projectRoot.resolve("python-analysis/langgraph_trading/analyze.py");
        return scriptPath.toString();
    }

    @Override
    public Signal analyze(double currentPrice) {
        // Add current price to history
        priceHistory.add(currentPrice);

        // Keep only the last N prices
        while (priceHistory.size() > maxHistorySize) {
            priceHistory.poll();
        }

        // Need at least 2 data points for analysis
        if (priceHistory.size() < 2) {
            logger.debug("Insufficient data points: {}", priceHistory.size());
            return Signal.HOLD;
        }

        try {
            // Build command to run Python script
            List<String> command = new ArrayList<>();
            command.add("python3");
            command.add(pythonScriptPath);
            command.add(String.valueOf(currentPrice));

            // Add price history as arguments
            for (Double price : priceHistory) {
                command.add(String.valueOf(price));
            }

            logger.debug("Executing Python analysis with {} price points", priceHistory.size());

            // Execute Python script
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            // Wait for process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.error("Python script exited with code: {}", exitCode);
                logger.error("Output: {}", output.toString());
                return Signal.HOLD;
            }

            // Parse JSON response
            return parseSignalFromJson(output.toString());

        } catch (Exception e) {
            logger.error("Error calling Python analysis", e);
            return Signal.HOLD;
        }
    }

    private Signal parseSignalFromJson(String jsonOutput) {
        try {
            JsonNode root = objectMapper.readTree(jsonOutput);

            String signalStr = root.get("signal").asText("HOLD");
            double confidence = root.get("confidence").asDouble(0.0);
            String reasoning = root.get("reasoning").asText("");

            logger.info("LangGraph signal: {} (confidence: {}%)", signalStr, confidence);
            logger.debug("Reasoning: {}", reasoning);

            switch (signalStr.toUpperCase()) {
                case "BUY":
                    return Signal.BUY;
                case "SELL":
                    return Signal.SELL;
                default:
                    return Signal.HOLD;
            }

        } catch (Exception e) {
            logger.error("Error parsing JSON response: {}", jsonOutput, e);
            return Signal.HOLD;
        }
    }

    public int getHistorySize() {
        return priceHistory.size();
    }

    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    public String getPythonScriptPath() {
        return pythonScriptPath;
    }
}
