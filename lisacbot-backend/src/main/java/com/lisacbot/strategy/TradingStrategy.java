package com.lisacbot.strategy;

import com.lisacbot.trading.Signal;

public interface TradingStrategy {
    Signal analyze(double currentPrice);
}
