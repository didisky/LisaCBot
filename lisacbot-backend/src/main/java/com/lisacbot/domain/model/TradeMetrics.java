package com.lisacbot.domain.model;

/**
 * Performance metrics calculated from trade history.
 */
public class TradeMetrics {
    private final int totalTrades;
    private final int buyTrades;
    private final int sellTrades;
    private final int profitableTrades;
    private final int losingTrades;
    private final double winRate;
    private final double totalProfitLoss;
    private final double averageProfitLoss;
    private final double bestTrade;
    private final double worstTrade;
    private final double totalVolume;
    private final String mostUsedStrategy;
    private final String mostProfitableStrategy;

    public TradeMetrics(
            int totalTrades,
            int buyTrades,
            int sellTrades,
            int profitableTrades,
            int losingTrades,
            double winRate,
            double totalProfitLoss,
            double averageProfitLoss,
            double bestTrade,
            double worstTrade,
            double totalVolume,
            String mostUsedStrategy,
            String mostProfitableStrategy
    ) {
        this.totalTrades = totalTrades;
        this.buyTrades = buyTrades;
        this.sellTrades = sellTrades;
        this.profitableTrades = profitableTrades;
        this.losingTrades = losingTrades;
        this.winRate = winRate;
        this.totalProfitLoss = totalProfitLoss;
        this.averageProfitLoss = averageProfitLoss;
        this.bestTrade = bestTrade;
        this.worstTrade = worstTrade;
        this.totalVolume = totalVolume;
        this.mostUsedStrategy = mostUsedStrategy;
        this.mostProfitableStrategy = mostProfitableStrategy;
    }

    // Getters
    public int getTotalTrades() {
        return totalTrades;
    }

    public int getBuyTrades() {
        return buyTrades;
    }

    public int getSellTrades() {
        return sellTrades;
    }

    public int getProfitableTrades() {
        return profitableTrades;
    }

    public int getLosingTrades() {
        return losingTrades;
    }

    public double getWinRate() {
        return winRate;
    }

    public double getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public double getAverageProfitLoss() {
        return averageProfitLoss;
    }

    public double getBestTrade() {
        return bestTrade;
    }

    public double getWorstTrade() {
        return worstTrade;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public String getMostUsedStrategy() {
        return mostUsedStrategy;
    }

    public String getMostProfitableStrategy() {
        return mostProfitableStrategy;
    }
}
