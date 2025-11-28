package com.lisacbot.domain.model;

/**
 * Result of a backtest execution.
 */
public class BacktestResult {
    private final double initialBalance;
    private final double finalBalance;
    private final int buyTrades;
    private final int sellTrades;
    private final int days;

    public BacktestResult(double initialBalance, double finalBalance, int buyTrades, int sellTrades, int days) {
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.buyTrades = buyTrades;
        this.sellTrades = sellTrades;
        this.days = days;
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
}
