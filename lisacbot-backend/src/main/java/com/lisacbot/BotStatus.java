package com.lisacbot;

/**
 * Data Transfer Object representing the current status of the trading bot.
 */
public class BotStatus {
    private boolean running;
    private double balance;
    private double holdings;
    private double currentPrice;  // Current Bitcoin price in USD
    private double totalValue;

    public BotStatus(boolean running, double balance, double holdings, double currentPrice, double totalValue) {
        this.running = running;
        this.balance = balance;
        this.holdings = holdings;
        this.currentPrice = currentPrice;
        this.totalValue = totalValue;
    }

    // Getters and setters
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getHoldings() {
        return holdings;
    }

    public void setHoldings(double holdings) {
        this.holdings = holdings;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
}
