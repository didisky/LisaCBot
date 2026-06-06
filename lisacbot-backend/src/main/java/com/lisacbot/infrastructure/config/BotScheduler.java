package com.lisacbot.infrastructure.config;

import com.lisacbot.domain.service.TradingService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

/**
 * Scheduler for executing trading cycles with dynamic interval configuration.
 */
@Component
public class BotScheduler {

    private final TradingService tradingService;
    private final ConfigurationService configurationService;
    private final TaskScheduler taskScheduler;

    private int currentPollIntervalSeconds;
    private ScheduledFuture<?> scheduledTask;

    public BotScheduler(TradingService tradingService, ConfigurationService configurationService) {
        this.tradingService = tradingService;
        this.configurationService = configurationService;

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("bot-scheduler-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    @PostConstruct
    public void initialize() {
        this.currentPollIntervalSeconds = configurationService.getPollIntervalSeconds();
        scheduleTask(currentPollIntervalSeconds);
    }

    /**
     * Updates the polling interval dynamically.
     * Cancels the current scheduled task and creates a new one with the new interval.
     */
    public synchronized void updatePollInterval(int newIntervalSeconds) {
        if (newIntervalSeconds < 10 || newIntervalSeconds > 300) {
            throw new IllegalArgumentException("Poll interval must be between 10 and 300 seconds");
        }

        // Cancel the current task if it exists
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        this.currentPollIntervalSeconds = newIntervalSeconds;
        configurationService.savePollIntervalSeconds(newIntervalSeconds);
        scheduleTask(newIntervalSeconds);
    }

    /**
     * Returns the current polling interval in seconds.
     */
    public int getCurrentPollInterval() {
        return currentPollIntervalSeconds;
    }

    private void scheduleTask(int intervalSeconds) {
        Duration period = Duration.ofSeconds(intervalSeconds);
        scheduledTask = taskScheduler.scheduleAtFixedRate(
            this::executeTradingCycle,
            period
        );
    }

    private void executeTradingCycle() {
        tradingService.executeTradingCycle();
    }
}
