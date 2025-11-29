package com.lisacbot.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model representing a trade (BUY or SELL operation).
 */
public class Trade {
    private final Long id;
    private final LocalDateTime timestamp;
    private final Signal type;
    private final double price;
    private final double quantity;
    private final double balanceBefore;
    private final double balanceAfter;
    private final Double profitLossPercentage;
    private final String strategy;
    private final MarketCycle marketCycle;
    private final String reason;

    public Trade(
            Long id,
            LocalDateTime timestamp,
            Signal type,
            double price,
            double quantity,
            double balanceBefore,
            double balanceAfter,
            Double profitLossPercentage,
            String strategy,
            MarketCycle marketCycle,
            String reason
    ) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.profitLossPercentage = profitLossPercentage;
        this.strategy = strategy;
        this.marketCycle = marketCycle;
        this.reason = reason;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Signal getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getBalanceBefore() {
        return balanceBefore;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public Double getProfitLossPercentage() {
        return profitLossPercentage;
    }

    public String getStrategy() {
        return strategy;
    }

    public MarketCycle getMarketCycle() {
        return marketCycle;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", price=" + price +
                ", quantity=" + quantity +
                ", profitLoss=" + profitLossPercentage +
                "%, strategy='" + strategy + '\'' +
                ", cycle=" + marketCycle +
                ", reason='" + reason + '\'' +
                '}';
    }
}
