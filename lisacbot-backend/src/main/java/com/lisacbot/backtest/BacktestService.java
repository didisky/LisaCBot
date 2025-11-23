package com.lisacbot.backtest;

import com.lisacbot.price.HistoricalPriceService;
import com.lisacbot.price.PriceData;
import com.lisacbot.strategy.TradingStrategy;
import com.lisacbot.trading.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to run backtests on trading strategies using historical price data.
 */
@Service
public class BacktestService {
    private static final Logger log = LoggerFactory.getLogger(BacktestService.class);

    private final HistoricalPriceService historicalPriceService;
    private final TradingStrategy strategy;

    @Value("${bot.backtest.days}")
    private int backtestDays;

    @Value("${bot.backtest.initial.balance}")
    private double initialBalance;

    public BacktestService(HistoricalPriceService historicalPriceService, TradingStrategy strategy) {
        this.historicalPriceService = historicalPriceService;
        this.strategy = strategy;
    }

    /**
     * Runs a backtest using the configured strategy and historical data.
     *
     * @return backtest results including P&L and trade statistics
     */
    public BacktestResult runBacktest() {
        return runBacktest(backtestDays, initialBalance);
    }

    /**
     * Runs a backtest with custom parameters.
     *
     * @param days number of days of historical data to use
     * @param startingBalance initial balance in USD
     * @return backtest results including P&L and trade statistics
     */
    public BacktestResult runBacktest(int days, double startingBalance) {
        log.info("Starting backtest for {} days with ${} initial balance", days, startingBalance);

        List<PriceData> historicalPrices = historicalPriceService.getHistoricalPrices(days);

        double balance = startingBalance;
        double holdings = 0.0;
        int buyTrades = 0;
        int sellTrades = 0;

        for (PriceData priceData : historicalPrices) {
            double price = priceData.price();
            Signal signal = strategy.analyze(price);

            switch (signal) {
                case BUY -> {
                    if (balance > 0) {
                        holdings = balance / price;
                        balance = 0;
                        buyTrades++;
                        log.debug("BUY at ${}: {} BTC", String.format("%.2f", price),
                                 String.format("%.6f", holdings));
                    }
                }
                case SELL -> {
                    if (holdings > 0) {
                        balance = holdings * price;
                        holdings = 0;
                        sellTrades++;
                        log.debug("SELL at ${}: ${}", String.format("%.2f", price),
                                 String.format("%.2f", balance));
                    }
                }
                case HOLD -> {
                    // No action
                }
            }
        }

        // Convert any remaining holdings to balance using last price
        if (holdings > 0 && !historicalPrices.isEmpty()) {
            double lastPrice = historicalPrices.get(historicalPrices.size() - 1).price();
            balance = holdings * lastPrice;
        }

        BacktestResult result = new BacktestResult(startingBalance, balance, buyTrades, sellTrades, days);
        log.info("Backtest completed: P&L = ${} ({}%)",
                String.format("%.2f", result.getProfitLoss()),
                String.format("%.2f", result.getProfitLossPercentage()));

        return result;
    }
}
