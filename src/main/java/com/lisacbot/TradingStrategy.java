package com.lisacbot;

public interface TradingStrategy {
    Signal analyze(double currentPrice);
}
