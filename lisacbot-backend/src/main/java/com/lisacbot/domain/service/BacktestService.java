package com.lisacbot.domain.service;

import com.lisacbot.domain.port.PriceProvider;
import com.lisacbot.domain.model.BacktestResult;
import com.lisacbot.domain.model.MarketCycle;
import com.lisacbot.domain.model.Portfolio;
import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for running backtests on trading strategies.
 * Reuses the core trading logic from TradingService.
 */
@Service
public class BacktestService {
    private static final Logger log = LoggerFactory.getLogger(BacktestService.class);

    private final PriceProvider priceProvider;
    private final TradingService tradingService;
    private final MarketCycleDetector cycleDetector;

    @Value("${bot.backtest.days}")
    private int defaultDays;

    @Value("${bot.backtest.initial.balance}")
    private double defaultInitialBalance;

    public BacktestService(
            PriceProvider priceProvider,
            TradingService tradingService,
            MarketCycleDetector cycleDetector
    ) {
        this.priceProvider = priceProvider;
        this.tradingService = tradingService;
        this.cycleDetector = cycleDetector;
    }

    public BacktestResult runBacktest() {
        return runBacktest(defaultDays, defaultInitialBalance);
    }

    public BacktestResult runBacktest(int days, double initialBalance) {
        log.info("Starting backtest for {} days with ${} initial balance", days, initialBalance);

        List<Price> historicalPrices = priceProvider.getHistoricalPrices(days);
        Portfolio backtestPortfolio = new Portfolio(initialBalance);

        // Detect the market cycle at the start of the backtest period
        // This gives a realistic simulation of the market conditions during the backtest
        MarketCycle backtestCycle = cycleDetector.detectCycle(historicalPrices);
        log.info("Backtest period market cycle detected: {}", backtestCycle);
        log.info("NOTE: Backtest will respect current allowed cycles configuration");

        int buyTrades = 0;
        int sellTrades = 0;

        // Execute trading cycle for each historical price point
        // This reuses the core trading logic including stop-loss, take-profit, and cycle checks from TradingService
        // The cycle check uses the current real-time cycle, which represents the strategy configuration being tested
        for (Price price : historicalPrices) {
            Signal executedSignal = tradingService.executeTradingCycle(price.value(), backtestPortfolio);

            // Track trades
            if (executedSignal == Signal.BUY && backtestPortfolio.hasHoldings()) {
                buyTrades++;
            } else if (executedSignal == Signal.SELL && !backtestPortfolio.hasHoldings()) {
                sellTrades++;
            }
        }

        // Convert remaining holdings to balance using last price
        if (backtestPortfolio.hasHoldings() && !historicalPrices.isEmpty()) {
            double lastPrice = historicalPrices.get(historicalPrices.size() - 1).value();
            backtestPortfolio.sell(lastPrice);
        }

        BacktestResult result = new BacktestResult(
                initialBalance,
                backtestPortfolio.getBalance(),
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
