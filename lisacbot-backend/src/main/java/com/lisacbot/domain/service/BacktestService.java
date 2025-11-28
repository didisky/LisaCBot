package com.lisacbot.domain.service;

import com.lisacbot.domain.port.PriceProvider;
import com.lisacbot.domain.model.BacktestResult;
import com.lisacbot.domain.model.Portfolio;
import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.model.Signal;
import com.lisacbot.domain.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for running backtests on trading strategies.
 */
@Service
public class BacktestService {
    private static final Logger log = LoggerFactory.getLogger(BacktestService.class);

    private final PriceProvider priceProvider;
    private final TradingStrategy strategy;

    @Value("${bot.backtest.days}")
    private int defaultDays;

    @Value("${bot.backtest.initial.balance}")
    private double defaultInitialBalance;

    public BacktestService(
            PriceProvider priceProvider,
            TradingStrategy strategy
    ) {
        this.priceProvider = priceProvider;
        this.strategy = strategy;
    }

    public BacktestResult runBacktest() {
        return runBacktest(defaultDays, defaultInitialBalance);
    }

    public BacktestResult runBacktest(int days, double initialBalance) {
        log.info("Starting backtest for {} days with ${} initial balance", days, initialBalance);

        List<Price> historicalPrices = priceProvider.getHistoricalPrices(days);
        Portfolio portfolio = new Portfolio(initialBalance);

        int buyTrades = 0;
        int sellTrades = 0;

        for (Price price : historicalPrices) {
            Signal signal = strategy.analyze(price.value());

            switch (signal) {
                case BUY -> {
                    if (portfolio.hasBalance()) {
                        portfolio.buy(price.value());
                        buyTrades++;
                        log.debug("BUY at ${}: {} BTC",
                                String.format("%.2f", price.value()),
                                String.format("%.6f", portfolio.getHoldings()));
                    }
                }
                case SELL -> {
                    if (portfolio.hasHoldings()) {
                        portfolio.sell(price.value());
                        sellTrades++;
                        log.debug("SELL at ${}: ${}",
                                String.format("%.2f", price.value()),
                                String.format("%.2f", portfolio.getBalance()));
                    }
                }
                case HOLD -> {
                    // No action
                }
            }
        }

        // Convert remaining holdings to balance using last price
        if (portfolio.hasHoldings() && !historicalPrices.isEmpty()) {
            double lastPrice = historicalPrices.get(historicalPrices.size() - 1).value();
            portfolio.sell(lastPrice);
        }

        BacktestResult result = new BacktestResult(
                initialBalance,
                portfolio.getBalance(),
                buyTrades,
                sellTrades,
                days
        );

        log.info("Backtest completed: P&L = ${} ({}%)",
                String.format("%.2f", result.getProfitLoss()),
                String.format("%.2f", result.getProfitLossPercentage()));

        return result;
    }
}
