package com.lisacbot.infrastructure.config;

import com.lisacbot.domain.service.TradingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for executing trading cycles.
 */
@Component
public class BotScheduler {

    private final TradingService tradingService;

    public BotScheduler(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @Scheduled(fixedRateString = "#{${bot.poll.interval.seconds} * 1000}")
    public void executeTradingCycle() {
        tradingService.executeTradingCycle();
    }
}
