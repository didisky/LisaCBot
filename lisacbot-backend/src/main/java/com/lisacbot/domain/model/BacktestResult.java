package com.lisacbot.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Result of a backtest execution.
 */
public class BacktestResult {
    private final double initialBalance;
    private final double finalBalance;
    private final int buyTrades;
    private final int sellTrades;
    private final int days;
    private final String strategyName;
    private final Map<String, String> strategyParameters;
    private final List<Trade> trades;

    public BacktestResult(double initialBalance, double finalBalance, int buyTrades, int sellTrades, int days,
                          String strategyName, Map<String, String> strategyParameters, List<Trade> trades) {
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.buyTrades = buyTrades;
        this.sellTrades = sellTrades;
        this.days = days;
        this.strategyName = strategyName;
        this.strategyParameters = strategyParameters;
        this.trades = trades;
    }

    public double getProfitLoss() {
        return finalBalance - initialBalance;
    }

    public double getProfitLossPercentage() {
        return ((finalBalance - initialBalance) / initialBalance) * 100;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public double getFinalBalance() {
        return finalBalance;
    }

    public int getBuyTrades() {
        return buyTrades;
    }

    public int getSellTrades() {
        return sellTrades;
    }

    public int getTotalTrades() {
        return buyTrades + sellTrades;
    }

    public int getDays() {
        return days;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public Map<String, String> getStrategyParameters() {
        return strategyParameters;
    }

    public List<Trade> getTrades() {
        return trades;
    }
}
