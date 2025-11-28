package com.lisacbot.infrastructure.config;

import com.lisacbot.domain.strategy.SimpleMovingAverageStrategy;
import com.lisacbot.domain.strategy.TradingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the trading bot.
 */
@Configuration
public class BotConfiguration {

    @Value("${bot.strategy.sma.period}")
    private int smaPeriod;

    @Bean
    public TradingStrategy tradingStrategy() {
        return new SimpleMovingAverageStrategy(smaPeriod);
    }
}
