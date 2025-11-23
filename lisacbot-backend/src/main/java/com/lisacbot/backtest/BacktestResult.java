package com.lisacbot.backtest;

/**
 * Results of a backtest execution.
 */
public class BacktestResult {
    private final double initialBalance;
    private final double finalBalance;
    private final int totalTrades;
    private final int buyTrades;
    private final int sellTrades;
    private final double profitLoss;
    private final double profitLossPercentage;
    private final int days;

    public BacktestResult(double initialBalance, double finalBalance, int buyTrades, int sellTrades, int days) {
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.buyTrades = buyTrades;
        this.sellTrades = sellTrades;
        this.totalTrades = buyTrades + sellTrades;
        this.profitLoss = finalBalance - initialBalance;
        this.profitLossPercentage = ((finalBalance - initialBalance) / initialBalance) * 100;
        this.days = days;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public double getFinalBalance() {
        return finalBalance;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public int getBuyTrades() {
        return buyTrades;
    }

    public int getSellTrades() {
        return sellTrades;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public double getProfitLossPercentage() {
        return profitLossPercentage;
    }

    public int getDays() {
        return days;
    }

    @Override
    public String toString() {
        return String.format("""

                ========== BACKTEST RESULTS ==========
                Period: %d days
                Initial Balance: $%.2f
                Final Balance: $%.2f
                Profit/Loss: $%.2f (%.2f%%)
                Total Trades: %d (Buy: %d, Sell: %d)
                ======================================
                """,
                days,
                initialBalance,
                finalBalance,
                profitLoss,
                profitLossPercentage,
                totalTrades,
                buyTrades,
                sellTrades
        );
    }
}
