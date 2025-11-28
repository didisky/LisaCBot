package com.lisacbot.domain.model;

/**
 * Represents a trading portfolio with cash balance and cryptocurrency holdings.
 */
public class Portfolio {
    private double balance;
    private double holdings;

    public Portfolio(double initialBalance) {
        this.balance = initialBalance;
        this.holdings = 0.0;
    }

    public void buy(double price) {
        if (balance > 0) {
            holdings = balance / price;
            balance = 0;
        }
    }

    public void sell(double price) {
        if (holdings > 0) {
            balance = holdings * price;
            holdings = 0;
        }
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

    public boolean hasBalance() {
        return balance > 0;
    }

    public boolean hasHoldings() {
        return holdings > 0;
    }
}
