package com.lisacbot.domain.service;

import com.lisacbot.domain.model.Signal;
import com.lisacbot.domain.model.Trade;
import com.lisacbot.domain.model.TradeMetrics;
import com.lisacbot.domain.port.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating trading performance metrics.
 */
@Service
public class MetricsService {

    private final TradeRepository tradeRepository;

    public MetricsService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    /**
     * Calculate comprehensive performance metrics from all trades.
     */
    public TradeMetrics calculateMetrics() {
        List<Trade> allTrades = tradeRepository.findAll();

        if (allTrades.isEmpty()) {
            return createEmptyMetrics();
        }

        // Filter SELL trades (only they have profit/loss data)
        List<Trade> sellTrades = allTrades.stream()
                .filter(t -> t.getType() == Signal.SELL)
                .filter(t -> t.getProfitLossPercentage() != null)
                .toList();

        int totalTrades = allTrades.size();
        int buyTrades = (int) allTrades.stream().filter(t -> t.getType() == Signal.BUY).count();
        int sellTradesCount = sellTrades.size();

        // Profit/loss analysis
        int profitableTrades = (int) sellTrades.stream()
                .filter(t -> t.getProfitLossPercentage() > 0)
                .count();
        int losingTrades = (int) sellTrades.stream()
                .filter(t -> t.getProfitLossPercentage() < 0)
                .count();

        double winRate = sellTradesCount > 0 
                ? (profitableTrades * 100.0) / sellTradesCount 
                : 0.0;

        double totalProfitLoss = sellTrades.stream()
                .mapToDouble(Trade::getProfitLossPercentage)
                .sum();

        double averageProfitLoss = sellTradesCount > 0
                ? totalProfitLoss / sellTradesCount
                : 0.0;

        double bestTrade = sellTrades.stream()
                .mapToDouble(Trade::getProfitLossPercentage)
                .max()
                .orElse(0.0);

        double worstTrade = sellTrades.stream()
                .mapToDouble(Trade::getProfitLossPercentage)
                .min()
                .orElse(0.0);

        // Volume calculation (sum of all trade values)
        double totalVolume = allTrades.stream()
                .mapToDouble(t -> t.getPrice() * t.getQuantity())
                .sum();

        // Strategy analysis
        String mostUsedStrategy = findMostUsedStrategy(allTrades);
        String mostProfitableStrategy = findMostProfitableStrategy(sellTrades);

        return new TradeMetrics(
                totalTrades,
                buyTrades,
                sellTradesCount,
                profitableTrades,
                losingTrades,
                winRate,
                totalProfitLoss,
                averageProfitLoss,
                bestTrade,
                worstTrade,
                totalVolume,
                mostUsedStrategy,
                mostProfitableStrategy
        );
    }

    private String findMostUsedStrategy(List<Trade> trades) {
        if (trades.isEmpty()) return "N/A";

        return trades.stream()
                .collect(Collectors.groupingBy(Trade::getStrategy, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private String findMostProfitableStrategy(List<Trade> sellTrades) {
        if (sellTrades.isEmpty()) return "N/A";

        Map<String, Double> profitByStrategy = sellTrades.stream()
                .collect(Collectors.groupingBy(
                        Trade::getStrategy,
                        Collectors.summingDouble(Trade::getProfitLossPercentage)
                ));

        return profitByStrategy.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private TradeMetrics createEmptyMetrics() {
        return new TradeMetrics(
                0, 0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "N/A", "N/A"
        );
    }
}
