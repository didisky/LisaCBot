package com.lisacbot.trading;

import com.lisacbot.price.PriceService;
import com.lisacbot.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class Bot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final PriceService priceService;
    private final TradingStrategy strategy;

    @Value("${bot.poll.interval.seconds}")
    private int pollInterval;

    private double balance = 1000.0;
    private double holdings = 0.0;

    public Bot(PriceService priceService, TradingStrategy strategy) {
        this.priceService = priceService;
        this.strategy = strategy;
    }

    @PostConstruct
    public void init() {
        log.info("LisaCBot started");
        log.info("Starting balance: ${}", balance);
        log.info("Checking prices every {} seconds", pollInterval);
    }

    /**
     * Main trading loop executed at configured interval.
     * Fetches current BTC price, analyzes it with the trading strategy,
     * and executes the resulting signal.
     */
    @Scheduled(fixedRateString = "#{${bot.poll.interval.seconds} * 1000}")
    public void tick() {
        try {
            double price = priceService.getBitcoinPrice();
            log.info("BTC price: ${}", String.format("%.2f", price));

            Signal signal = strategy.analyze(price);
            executeSignal(signal, price);

        } catch (Exception e) {
            log.error("Error fetching price: {}", e.getMessage());
        }
    }

    /**
     * Executes a trading signal by updating balance and holdings.
     * BUY converts all USD balance to BTC, SELL converts all BTC to USD.
     *
     * @param signal the trading signal to execute
     * @param price  current BTC price in USD
     */
    private void executeSignal(Signal signal, double price) {
        switch (signal) {
            case BUY -> {
                if (balance > 0) {
                    holdings = balance / price;
                    balance = 0;
                    log.info("BUY: Bought {} BTC", String.format("%.6f", holdings));
                }
            }
            case SELL -> {
                if (holdings > 0) {
                    balance = holdings * price;
                    holdings = 0;
                    log.info("SELL: Sold for ${}", String.format("%.2f", balance));
                }
            }
            case HOLD -> log.info("HOLD");
        }

        double totalValue = balance + (holdings * price);
        log.info("Portfolio value: ${}", String.format("%.2f", totalValue));
    }
}
