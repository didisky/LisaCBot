package com.lisacbot.infrastructure.persistence;

import com.lisacbot.domain.model.MarketCycle;
import com.lisacbot.domain.model.Signal;
import com.lisacbot.domain.model.Trade;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity for persisting trades in PostgreSQL.
 */
@Entity
@Table(name = "trades")
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Signal type;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false)
    private double balanceBefore;

    @Column(nullable = false)
    private double balanceAfter;

    @Column
    private Double profitLossPercentage;

    @Column(nullable = false, length = 50)
    private String strategy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarketCycle marketCycle;

    @Column(length = 100)
    private String reason;

    // Default constructor for JPA
    protected TradeEntity() {
    }

    public TradeEntity(
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

    /**
     * Converts this JPA entity to a domain model.
     */
    public Trade toDomain() {
        return new Trade(
                id,
                timestamp,
                type,
                price,
                quantity,
                balanceBefore,
                balanceAfter,
                profitLossPercentage,
                strategy,
                marketCycle,
                reason
        );
    }

    /**
     * Creates a JPA entity from a domain model.
     */
    public static TradeEntity fromDomain(Trade trade) {
        return new TradeEntity(
                trade.getTimestamp(),
                trade.getType(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getBalanceBefore(),
                trade.getBalanceAfter(),
                trade.getProfitLossPercentage(),
                trade.getStrategy(),
                trade.getMarketCycle(),
                trade.getReason()
        );
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Signal getType() {
        return type;
    }

    public void setType(Signal type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(double balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Double getProfitLossPercentage() {
        return profitLossPercentage;
    }

    public void setProfitLossPercentage(Double profitLossPercentage) {
        this.profitLossPercentage = profitLossPercentage;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public MarketCycle getMarketCycle() {
        return marketCycle;
    }

    public void setMarketCycle(MarketCycle marketCycle) {
        this.marketCycle = marketCycle;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
