package com.lisacbot.domain.model;

/**
 * Represents a trading portfolio with cash balance and cryptocurrency holdings.
 */
public class Portfolio {
    private double balance;
    private double holdings;
    private double buyPrice;  // Track the price at which we bought
    private double highestPriceSinceEntry;  // Track highest price for trailing stop-loss

    public Portfolio(double initialBalance) {
        this.balance = initialBalance;
        this.holdings = 0.0;
        this.buyPrice = 0.0;
        this.highestPriceSinceEntry = 0.0;
    }

    public void buy(double price) {
        if (balance > 0) {
            holdings = balance / price;
            balance = 0;
            buyPrice = price;  // Remember buy price for stop-loss
            highestPriceSinceEntry = price;  // Initialize highest price at entry
        }
    }

    public void sell(double price) {
        if (holdings > 0) {
            balance = holdings * price;
            holdings = 0;
            buyPrice = 0.0;  // Reset buy price
            highestPriceSinceEntry = 0.0;  // Reset highest price
        }
    }

    /**
     * Checks if stop-loss should be triggered.
     *
     * @param currentPrice current market price
     * @param stopLossPercentage maximum loss percentage (e.g., 5.0 for 5%)
     * @return true if current loss exceeds stop-loss threshold
     */
    public boolean shouldTriggerStopLoss(double currentPrice, double stopLossPercentage) {
        if (!hasHoldings() || buyPrice == 0.0) {
            return false;
        }

        double lossPercentage = ((buyPrice - currentPrice) / buyPrice) * 100.0;
        return lossPercentage >= stopLossPercentage;
    }

    /**
     * Updates the highest price seen since entry.
     * This is used for trailing stop-loss calculation.
     *
     * @param currentPrice current market price
     */
    public void updateHighestPrice(double currentPrice) {
        if (hasHoldings() && currentPrice > highestPriceSinceEntry) {
            highestPriceSinceEntry = currentPrice;
        }
    }

    /**
     * Checks if trailing stop-loss should be triggered.
     * Unlike fixed stop-loss, trailing stop-loss follows the price upward.
     * The stop-loss is calculated from the highest price reached, not the buy price.
     *
     * @param currentPrice current market price
     * @param trailingStopLossPercentage maximum drop from peak (e.g., 5.0 for 5%)
     * @return true if current price dropped below trailing stop threshold
     */
    public boolean shouldTriggerTrailingStopLoss(double currentPrice, double trailingStopLossPercentage) {
        if (!hasHoldings() || highestPriceSinceEntry == 0.0) {
            return false;
        }

        // Calculate drop percentage from the highest price (not buy price)
        double dropPercentage = ((highestPriceSinceEntry - currentPrice) / highestPriceSinceEntry) * 100.0;
        return dropPercentage >= trailingStopLossPercentage;
    }

    /**
     * Checks if take-profit should be triggered.
     *
     * @param currentPrice current market price
     * @param takeProfitPercentage target profit percentage (e.g., 8.0 for 8%)
     * @return true if current profit exceeds take-profit threshold
     */
    public boolean shouldTriggerTakeProfit(double currentPrice, double takeProfitPercentage) {
        if (!hasHoldings() || buyPrice == 0.0) {
            return false;
        }

        double profitPercentage = ((currentPrice - buyPrice) / buyPrice) * 100.0;
        return profitPercentage >= takeProfitPercentage;
    }

    public double getTotalValue(double currentPrice) {
        return balance + (holdings * currentPrice);
    }

    public double getBalance() {
        return balance;
    }

    public double getHoldings() {
        return holdings;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getHighestPriceSinceEntry() {
        return highestPriceSinceEntry;
    }

    public boolean hasBalance() {
        return balance > 0;
    }

    public boolean hasHoldings() {
        return holdings > 0;
    }

    /**
     * Calculates current profit/loss percentage if holding.
     *
     * @param currentPrice current market price
     * @return profit/loss percentage, or 0 if not holding
     */
    public double getCurrentProfitLossPercentage(double currentPrice) {
        if (!hasHoldings() || buyPrice == 0.0) {
            return 0.0;
        }
        return ((currentPrice - buyPrice) / buyPrice) * 100.0;
    }
}
