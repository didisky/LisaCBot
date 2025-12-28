package com.lisacbot.domain.service;

import com.lisacbot.domain.port.PriceProvider;
import com.lisacbot.domain.model.BacktestResult;
import com.lisacbot.domain.model.MarketCycle;
import com.lisacbot.domain.model.Portfolio;
import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.model.Signal;
import com.lisacbot.domain.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        log.info("Retrieved {} historical price points from provider", historicalPrices.size());

        if (historicalPrices.isEmpty()) {
            log.error("No historical prices available for backtest!");
            throw new RuntimeException("No historical price data available");
        }

        Portfolio backtestPortfolio = new Portfolio(initialBalance);

        // Detect the market cycle at the start of the backtest period
        // This is for informational purposes only - backtest runs independently of current market conditions
        MarketCycle backtestCycle = cycleDetector.detectCycle(historicalPrices);
        log.info("Backtest period market cycle detected: {}", backtestCycle);

        int buyTrades = 0;
        int sellTrades = 0;
        List<Trade> trades = new ArrayList<>();
        String strategyName = tradingService.getStrategyName();

        log.info("Backtest will test strategy independently of current market cycle");
        log.info("Historical backtest period cycle: {}", backtestCycle);

        // Calculate the time interval between price points for realistic timestamps
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        long intervalMinutes = (days * 24 * 60) / Math.max(1, historicalPrices.size());

        // Execute trading cycle for each historical price point
        // Uses backtest-specific method that skips current market cycle check
        // Still performs trailing stop-loss and take-profit checks
        for (int i = 0; i < historicalPrices.size(); i++) {
            Price price = historicalPrices.get(i);
            double balanceBefore = backtestPortfolio.getBalance();
            double holdingsBefore = backtestPortfolio.getHoldings();

            Signal executedSignal = tradingService.executeTradingCycleForBacktest(price.value(), backtestPortfolio);

            // Track trades and create trade records
            // Only record a trade if the holdings actually changed (trade was executed)
            if (executedSignal == Signal.BUY && holdingsBefore == 0 && backtestPortfolio.hasHoldings()) {
                buyTrades++;
                LocalDateTime tradeTime = startTime.plusMinutes(i * intervalMinutes);
                Trade trade = new Trade(
                        null,
                        tradeTime,
                        Signal.BUY,
                        price.value(),
                        backtestPortfolio.getHoldings(),
                        balanceBefore,
                        backtestPortfolio.getBalance(),
                        null, // No P&L on buy
                        strategyName,
                        backtestCycle,
                        "Backtest signal"
                );
                trades.add(trade);
            } else if (executedSignal == Signal.SELL && holdingsBefore > 0 && !backtestPortfolio.hasHoldings()) {
                sellTrades++;
                // Calculate P&L for sell trades
                Double profitLoss = null;
                if (holdingsBefore > 0) {
                    double costBasis = balanceBefore; // Balance before sell was 0, so cost was from previous buy
                    double revenue = backtestPortfolio.getBalance();
                    // Find the cost from the last BUY trade
                    for (int j = trades.size() - 1; j >= 0; j--) {
                        if (trades.get(j).getType() == Signal.BUY) {
                            costBasis = trades.get(j).getBalanceBefore();
                            profitLoss = ((revenue - costBasis) / costBasis) * 100;
                            break;
                        }
                    }
                }

                LocalDateTime tradeTime = startTime.plusMinutes(i * intervalMinutes);
                Trade trade = new Trade(
                        null,
                        tradeTime,
                        Signal.SELL,
                        price.value(),
                        holdingsBefore,
                        balanceBefore,
                        backtestPortfolio.getBalance(),
                        profitLoss,
                        strategyName,
                        backtestCycle,
                        "Backtest signal"
                );
                trades.add(trade);
            }
        }

        log.info("Backtest loop completed: {} BUY signals, {} SELL signals, {} total trades recorded",
                buyTrades, sellTrades, trades.size());

        // Convert remaining holdings to balance using last price
        if (backtestPortfolio.hasHoldings() && !historicalPrices.isEmpty()) {
            double lastPrice = historicalPrices.get(historicalPrices.size() - 1).value();
            backtestPortfolio.sell(lastPrice);
        }

        // Get strategy parameters for the backtest result
        java.util.Map<String, String> strategyParameters = tradingService.getStrategyParameters();

        BacktestResult result = new BacktestResult(
                initialBalance,
                backtestPortfolio.getBalance(),
                buyTrades,
                sellTrades,
                days,
                strategyName,
                strategyParameters,
                trades
        );

        log.info("Backtest completed: Strategy={}, P&L = ${} ({}%), Trades executed: {}",
                strategyName,
                String.format("%.2f", result.getProfitLoss()),
                String.format("%.2f", result.getProfitLossPercentage()),
                trades.size());

        return result;
    }
}
