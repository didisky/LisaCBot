package com.lisacbot.domain.service;

import com.lisacbot.domain.port.PriceProvider;
import com.lisacbot.domain.port.TradeRepository;
import com.lisacbot.domain.model.BotStatus;
import com.lisacbot.domain.model.MarketCycle;
import com.lisacbot.domain.model.Portfolio;
import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.model.Signal;
import com.lisacbot.domain.model.Trade;
import com.lisacbot.domain.strategy.TradingStrategy;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core trading service implementing the trading business logic.
 */
@Service
public class TradingService {
    private static final Logger log = LoggerFactory.getLogger(TradingService.class);

    private final PriceProvider priceProvider;
    private TradingStrategy strategy; // Non-final to allow runtime strategy switching
    private final MarketCycleDetector cycleDetector;
    private final TradeRepository tradeRepository;
    private final TradeEventPublisher tradeEventPublisher;
    private final Portfolio portfolio;
    private final boolean trailingStopLossEnabled;
    private final double trailingStopLossPercentage;
    private final boolean takeProfitEnabled;
    private final double takeProfitPercentage;
    private final int cycleAnalysisDays;
    private final Set<MarketCycle> allowedCycles;
    private String strategyName; // Non-final to allow runtime strategy name updates

    private Price lastPrice;
    private boolean running;
    private MarketCycle currentMarketCycle;

    public TradingService(
            PriceProvider priceProvider,
            TradingStrategy strategy,
            MarketCycleDetector cycleDetector,
            TradeRepository tradeRepository,
            TradeEventPublisher tradeEventPublisher,
            @Value("${bot.initial.balance}") double initialBalance,
            @Value("${bot.trailing.stop.loss.enabled}") boolean trailingStopLossEnabled,
            @Value("${bot.trailing.stop.loss.percentage}") double trailingStopLossPercentage,
            @Value("${bot.take.profit.enabled}") boolean takeProfitEnabled,
            @Value("${bot.take.profit.percentage}") double takeProfitPercentage,
            @Value("${bot.cycle.analysis.window.days}") int cycleAnalysisDays,
            @Value("${bot.cycle.allowed}") String allowedCyclesConfig,
            @Value("${bot.strategy.type}") String strategyName
    ) {
        this.priceProvider = priceProvider;
        this.strategy = strategy;
        this.cycleDetector = cycleDetector;
        this.tradeRepository = tradeRepository;
        this.tradeEventPublisher = tradeEventPublisher;
        this.portfolio = new Portfolio(initialBalance);
        this.trailingStopLossEnabled = trailingStopLossEnabled;
        this.trailingStopLossPercentage = trailingStopLossPercentage;
        this.takeProfitEnabled = takeProfitEnabled;
        this.takeProfitPercentage = takeProfitPercentage;
        this.cycleAnalysisDays = cycleAnalysisDays;
        this.strategyName = strategyName;

        // Parse allowed cycles from comma-separated config
        this.allowedCycles = Arrays.stream(allowedCyclesConfig.split(","))
                .map(String::trim)
                .map(MarketCycle::valueOf)
                .collect(Collectors.toSet());

        this.running = false;
        this.currentMarketCycle = MarketCycle.UNKNOWN;
    }

    @PostConstruct
    public void initialize() {
        log.info("LisaCBot initialized (not started)");
        log.info("Starting balance: ${}", portfolio.getBalance());
        if (trailingStopLossEnabled) {
            log.info("Trailing stop-loss enabled: {}%", trailingStopLossPercentage);
        }
        if (takeProfitEnabled) {
            log.info("Take-profit enabled: {}%", takeProfitPercentage);
        }
        log.info("Allowed market cycles for trading: {}", allowedCycles);

        // Initial market cycle detection
        updateMarketCycle();
    }

    public synchronized void start() {
        if (!running) {
            running = true;
            log.info("LisaCBot STARTED - Trading enabled");
        } else {
            log.warn("LisaCBot already running");
        }
    }

    public synchronized void stop() {
        if (running) {
            running = false;
            log.info("LisaCBot STOPPED - Trading disabled");
        } else {
            log.warn("LisaCBot already stopped");
        }
    }

    public void executeTradingCycle() {
        if (!running) {
            log.debug("Trading cycle skipped - bot is stopped");
            return;
        }

        try {
            lastPrice = priceProvider.getCurrentPrice();
            executeTradingCycle(lastPrice.value());
        } catch (Exception e) {
            log.error("Error during trading cycle: {}", e.getMessage());
        }
    }

    /**
     * Scheduled task to periodically update the market cycle.
     * Runs at the configured interval (default: every 24 hours).
     * The fixedRateString uses milliseconds, so we convert hours to ms: hours * 60 * 60 * 1000
     */
    @Scheduled(fixedRateString = "#{${bot.cycle.update.interval.hours} * 60 * 60 * 1000}")
    public void scheduledMarketCycleUpdate() {
        log.info("Scheduled market cycle update triggered");
        updateMarketCycle();
    }

    /**
     * Updates the current market cycle by analyzing historical price data.
     * This should be called periodically (e.g., once per day) as cycle detection requires time.
     * If the new cycle is not allowed and we have holdings, triggers an automatic sell.
     */
    public void updateMarketCycle() {
        try {
            log.info("Updating market cycle analysis...");
            List<Price> historicalPrices = priceProvider.getHistoricalPrices(cycleAnalysisDays);
            MarketCycle previousCycle = currentMarketCycle;
            currentMarketCycle = cycleDetector.detectCycle(historicalPrices);

            log.info("Market cycle updated: {} -> {}", previousCycle, currentMarketCycle);

            // Check if we entered a non-allowed cycle
            if (!isTradingAllowed()) {
                log.warn("Entered non-allowed market cycle: {}. Trading is now DISABLED.", currentMarketCycle);

                // If we have holdings, sell immediately
                if (portfolio.hasHoldings() && lastPrice != null) {
                    log.warn("CYCLE PROTECTION: Selling holdings due to non-allowed market cycle");
                    executeSignal(Signal.SELL, lastPrice.value(), portfolio, "Cycle protection");
                }
            } else {
                log.info("Trading is ALLOWED in current market cycle: {}", currentMarketCycle);
            }
        } catch (Exception e) {
            log.error("Error updating market cycle: {}", e.getMessage());
            currentMarketCycle = MarketCycle.UNKNOWN;
        }
    }

    /**
     * Checks if trading is allowed in the current market cycle.
     *
     * @return true if current cycle is in the allowed cycles list
     */
    private boolean isTradingAllowed() {
        return allowedCycles.contains(currentMarketCycle);
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

        // Update highest price for trailing stop-loss calculation
        portfolio.updateHighestPrice(price);

        // Risk management checks (priority over strategy)

        // 1. Check trailing stop-loss (protect against losses and secure profits)
        if (trailingStopLossEnabled && portfolio.shouldTriggerTrailingStopLoss(price, trailingStopLossPercentage)) {
            double profitLoss = portfolio.getCurrentProfitLossPercentage(price);
            double highestPrice = portfolio.getHighestPriceSinceEntry();
            log.warn("TRAILING STOP-LOSS TRIGGERED! Peak: ${}, Current: ${}, P/L: {}%",
                    String.format("%.2f", highestPrice),
                    String.format("%.2f", price),
                    String.format("%.2f", profitLoss));
            executeSignal(Signal.SELL, price, portfolio, "Trailing stop-loss");
            return Signal.SELL;
        }

        // 2. Check take-profit (secure gains)
        if (takeProfitEnabled && portfolio.shouldTriggerTakeProfit(price, takeProfitPercentage)) {
            double profit = portfolio.getCurrentProfitLossPercentage(price);
            log.info("TAKE-PROFIT TRIGGERED! Profit: +{}%", String.format("%.2f", profit));
            executeSignal(Signal.SELL, price, portfolio, "Take profit");
            return Signal.SELL;
        }

        // 3. Check if trading is allowed in current market cycle
        if (!isTradingAllowed()) {
            log.info("Trading DISABLED - current market cycle not allowed: {}", currentMarketCycle);

            // If we somehow have holdings in a non-allowed cycle, sell them
            if (portfolio.hasHoldings()) {
                log.warn("CYCLE PROTECTION: Selling holdings in non-allowed cycle");
                executeSignal(Signal.SELL, price, portfolio, "Cycle protection");
                return Signal.SELL;
            }

            // Otherwise just hold (no trading)
            log.info("HOLD (cycle protection)");
            return Signal.HOLD;
        }

        // 4. Normal strategy analysis (only if cycle is allowed)
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
        executeSignal(signal, price, portfolio, "Strategy signal");
    }

    private void executeSignal(Signal signal, double price, Portfolio portfolio, String reason) {
        double balanceBefore = portfolio.getBalance();
        double holdingsBefore = portfolio.getHoldings();
        Double profitLoss = null;

        switch (signal) {
            case BUY -> {
                if (portfolio.hasBalance()) {
                    portfolio.buy(price);
                    log.info("BUY: Bought {} BTC at ${}",
                            String.format("%.6f", portfolio.getHoldings()),
                            String.format("%.2f", price));

                    // Persist trade (only for real trading, not backtest)
                    if (portfolio == this.portfolio) {
                        persistTrade(signal, price, portfolio.getHoldings(), balanceBefore, portfolio.getBalance(), null, reason);
                    }
                }
            }
            case SELL -> {
                if (portfolio.hasHoldings()) {
                    profitLoss = portfolio.getCurrentProfitLossPercentage(price);
                    portfolio.sell(price);
                    log.info("SELL: Sold for ${} (P/L: {}%)",
                            String.format("%.2f", portfolio.getBalance()),
                            String.format("%.2f", profitLoss));

                    // Persist trade (only for real trading, not backtest)
                    if (portfolio == this.portfolio) {
                        persistTrade(signal, price, holdingsBefore, balanceBefore, portfolio.getBalance(), profitLoss, reason);
                    }
                }
            }
            case HOLD -> {
                log.info("HOLD");
                if (portfolio.hasHoldings()) {
                    profitLoss = portfolio.getCurrentProfitLossPercentage(price);
                    log.info("Current P/L: {}%", String.format("%.2f", profitLoss));
                }
            }
        }

        double totalValue = portfolio.getTotalValue(price);
        log.info("Portfolio value: ${}", String.format("%.2f", totalValue));
    }

    private void persistTrade(Signal signal, double price, double quantity, double balanceBefore, double balanceAfter, Double profitLoss, String reason) {
        Trade trade = new Trade(
                null, // ID will be generated by database
                LocalDateTime.now(),
                signal,
                price,
                quantity,
                balanceBefore,
                balanceAfter,
                profitLoss,
                strategyName,
                currentMarketCycle,
                reason
        );
        Trade savedTrade = tradeRepository.save(trade);
        log.debug("Trade persisted: {}", savedTrade);

        // Publish event for real-time notification
        tradeEventPublisher.publishTradeEvent(savedTrade);
    }

    public BotStatus getBotStatus() {
        double currentPrice = lastPrice != null ? lastPrice.value() : 0.0;
        double totalValue = portfolio.getTotalValue(currentPrice);

        return new BotStatus(
                running,
                portfolio.getBalance(),
                portfolio.getHoldings(),
                currentPrice,
                totalValue,
                currentMarketCycle,
                strategyName
        );
    }

    /**
     * Updates the trading strategy at runtime.
     * This allows dynamic strategy switching without restarting the bot.
     *
     * @param newStrategy the new strategy to use
     * @param newStrategyName the name of the new strategy
     */
    public synchronized void updateStrategy(TradingStrategy newStrategy, String newStrategyName) {
        log.info("Updating trading strategy from {} to {}", this.strategyName, newStrategyName);
        this.strategy = newStrategy;
        this.strategyName = newStrategyName;
        log.info("Trading strategy updated successfully");
    }
}
