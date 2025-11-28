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

    private Price lastPrice;
    private boolean running;

    public TradingService(
            PriceProvider priceProvider,
            TradingStrategy strategy,
            @Value("${bot.initial.balance}") double initialBalance
    ) {
        this.priceProvider = priceProvider;
        this.strategy = strategy;
        this.portfolio = new Portfolio(initialBalance);
        this.running = false;
    }

    @PostConstruct
    public void start() {
        running = true;
        log.info("LisaCBot started");
        log.info("Starting balance: ${}", portfolio.getBalance());
    }

    public void executeTradingCycle() {
        try {
            lastPrice = priceProvider.getCurrentPrice();
            log.info("BTC price: ${}", String.format("%.2f", lastPrice.value()));

            Signal signal = strategy.analyze(lastPrice.value());
            executeSignal(signal);

        } catch (Exception e) {
            log.error("Error during trading cycle: {}", e.getMessage());
        }
    }

    private void executeSignal(Signal signal) {
        switch (signal) {
            case BUY -> {
                if (portfolio.hasBalance()) {
                    portfolio.buy(lastPrice.value());
                    log.info("BUY: Bought {} BTC", String.format("%.6f", portfolio.getHoldings()));
                }
            }
            case SELL -> {
                if (portfolio.hasHoldings()) {
                    portfolio.sell(lastPrice.value());
                    log.info("SELL: Sold for ${}", String.format("%.2f", portfolio.getBalance()));
                }
            }
            case HOLD -> log.info("HOLD");
        }

        double totalValue = portfolio.getTotalValue(lastPrice.value());
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
