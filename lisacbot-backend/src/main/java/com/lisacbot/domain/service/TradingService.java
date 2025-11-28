package com.lisacbot.domain.service;

import com.lisacbot.domain.port.PriceProvider;
import com.lisacbot.domain.model.BotStatus;
import com.lisacbot.domain.model.Portfolio;
import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.model.Signal;
import com.lisacbot.domain.strategy.TradingStrategy;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Core trading service implementing the trading business logic.
 */
@Service
public class TradingService {
    private static final Logger log = LoggerFactory.getLogger(TradingService.class);

    private final PriceProvider priceProvider;
    private final TradingStrategy strategy;
    private final Portfolio portfolio;
    private final boolean stopLossEnabled;
    private final double stopLossPercentage;
    private final boolean takeProfitEnabled;
    private final double takeProfitPercentage;

    private Price lastPrice;
    private boolean running;

    public TradingService(
            PriceProvider priceProvider,
            TradingStrategy strategy,
            @Value("${bot.initial.balance}") double initialBalance,
            @Value("${bot.stop.loss.enabled}") boolean stopLossEnabled,
            @Value("${bot.stop.loss.percentage}") double stopLossPercentage,
            @Value("${bot.take.profit.enabled}") boolean takeProfitEnabled,
            @Value("${bot.take.profit.percentage}") double takeProfitPercentage
    ) {
        this.priceProvider = priceProvider;
        this.strategy = strategy;
        this.portfolio = new Portfolio(initialBalance);
        this.stopLossEnabled = stopLossEnabled;
        this.stopLossPercentage = stopLossPercentage;
        this.takeProfitEnabled = takeProfitEnabled;
        this.takeProfitPercentage = takeProfitPercentage;
        this.running = false;
    }

    @PostConstruct
    public void start() {
        running = true;
        log.info("LisaCBot started");
        log.info("Starting balance: ${}", portfolio.getBalance());
        if (stopLossEnabled) {
            log.info("Stop-loss enabled: {}%", stopLossPercentage);
        }
        if (takeProfitEnabled) {
            log.info("Take-profit enabled: {}%", takeProfitPercentage);
        }
    }

    public void executeTradingCycle() {
        try {
            lastPrice = priceProvider.getCurrentPrice();
            executeTradingCycle(lastPrice.value());
        } catch (Exception e) {
            log.error("Error during trading cycle: {}", e.getMessage());
        }
    }

    /**
     * Executes a trading cycle with a given price.
     * This method contains the core trading logic including stop-loss and take-profit checks.
     * Can be called by real-time trading or backtesting.
     *
     * @param price the current price to use for trading decisions
     * @param portfolio the portfolio to operate on
     * @return the signal that was executed (BUY, SELL, or HOLD)
     */
    public Signal executeTradingCycle(double price, Portfolio portfolio) {
        log.info("BTC price: ${}", String.format("%.2f", price));

        // Risk management checks (priority over strategy)

        // 1. Check stop-loss first (protect against losses)
        if (stopLossEnabled && portfolio.shouldTriggerStopLoss(price, stopLossPercentage)) {
            double loss = portfolio.getCurrentProfitLossPercentage(price);
            log.warn("STOP-LOSS TRIGGERED! Loss: {}%", String.format("%.2f", loss));
            executeSignal(Signal.SELL, price, portfolio);
            return Signal.SELL;
        }

        // 2. Check take-profit (secure gains)
        if (takeProfitEnabled && portfolio.shouldTriggerTakeProfit(price, takeProfitPercentage)) {
            double profit = portfolio.getCurrentProfitLossPercentage(price);
            log.info("TAKE-PROFIT TRIGGERED! Profit: +{}%", String.format("%.2f", profit));
            executeSignal(Signal.SELL, price, portfolio);
            return Signal.SELL;
        }

        // 3. Normal strategy analysis
        Signal signal = strategy.analyze(price);
        executeSignal(signal, price, portfolio);
        return signal;
    }

    /**
     * Executes a trading cycle with the service's own portfolio.
     *
     * @param price the current price
     */
    private void executeTradingCycle(double price) {
        executeTradingCycle(price, this.portfolio);
    }

    private void executeSignal(Signal signal, double price, Portfolio portfolio) {
        switch (signal) {
            case BUY -> {
                if (portfolio.hasBalance()) {
                    portfolio.buy(price);
                    log.info("BUY: Bought {} BTC at ${}",
                            String.format("%.6f", portfolio.getHoldings()),
                            String.format("%.2f", price));
                }
            }
            case SELL -> {
                if (portfolio.hasHoldings()) {
                    double profitLoss = portfolio.getCurrentProfitLossPercentage(price);
                    portfolio.sell(price);
                    log.info("SELL: Sold for ${} (P/L: {}%)",
                            String.format("%.2f", portfolio.getBalance()),
                            String.format("%.2f", profitLoss));
                }
            }
            case HOLD -> {
                log.info("HOLD");
                if (portfolio.hasHoldings()) {
                    double profitLoss = portfolio.getCurrentProfitLossPercentage(price);
                    log.info("Current P/L: {}%", String.format("%.2f", profitLoss));
                }
            }
        }

        double totalValue = portfolio.getTotalValue(price);
        log.info("Portfolio value: ${}", String.format("%.2f", totalValue));
    }

    public BotStatus getBotStatus() {
        double currentPrice = lastPrice != null ? lastPrice.value() : 0.0;
        double totalValue = portfolio.getTotalValue(currentPrice);

        return new BotStatus(
                running,
                portfolio.getBalance(),
                portfolio.getHoldings(),
                currentPrice,
                totalValue
        );
    }
}
