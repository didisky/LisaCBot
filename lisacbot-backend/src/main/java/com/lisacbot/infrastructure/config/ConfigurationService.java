package com.lisacbot.infrastructure.config;

import com.lisacbot.infrastructure.persistence.BotConfigEntity;
import com.lisacbot.infrastructure.persistence.JpaBotConfigRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for managing runtime configuration parameters.
 * Values are persisted in the bot_config DB table and survive restarts.
 * Falls back to application.properties defaults on first startup.
 */
@Service
public class ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    // Keys
    private static final String KEY_STRATEGY_TYPE             = "strategy.type";
    private static final String KEY_POLL_INTERVAL             = "poll.interval.seconds";
    private static final String KEY_INITIAL_BALANCE           = "initial.balance";
    private static final String KEY_TRAILING_SL_ENABLED       = "trailing.stop.loss.enabled";
    private static final String KEY_TRAILING_SL_PERCENTAGE    = "trailing.stop.loss.percentage";
    private static final String KEY_TAKE_PROFIT_ENABLED       = "take.profit.enabled";
    private static final String KEY_TAKE_PROFIT_PERCENTAGE    = "take.profit.percentage";
    private static final String KEY_CYCLE_ALLOWED             = "cycle.allowed";
    private static final String KEY_BACKTEST_DAYS             = "backtest.days";
    private static final String KEY_BACKTEST_INITIAL_BALANCE  = "backtest.initial.balance";
    private static final String KEY_SMA_PERIOD               = "sma.period";
    private static final String KEY_EMA_PERIOD               = "ema.period";
    private static final String KEY_RSI_PERIOD               = "rsi.period";
    private static final String KEY_RSI_OVERSOLD             = "rsi.oversold";
    private static final String KEY_RSI_OVERBOUGHT           = "rsi.overbought";
    private static final String KEY_MACD_FAST_PERIOD         = "macd.fast.period";
    private static final String KEY_MACD_SLOW_PERIOD         = "macd.slow.period";
    private static final String KEY_MACD_SIGNAL_PERIOD       = "macd.signal.period";
    private static final String KEY_COMPOSITE_BUY_THRESHOLD  = "composite.buy.threshold";
    private static final String KEY_COMPOSITE_SELL_THRESHOLD = "composite.sell.threshold";

    // Defaults from application.properties
    @Value("${bot.strategy.type}")
    private String defaultStrategyType;
    @Value("${bot.poll.interval.seconds}")
    private int defaultPollIntervalSeconds;
    @Value("${bot.initial.balance}")
    private double defaultInitialBalance;
    @Value("${bot.trailing.stop.loss.enabled}")
    private boolean defaultTrailingStopLossEnabled;
    @Value("${bot.trailing.stop.loss.percentage}")
    private double defaultTrailingStopLossPercentage;
    @Value("${bot.take.profit.enabled}")
    private boolean defaultTakeProfitEnabled;
    @Value("${bot.take.profit.percentage}")
    private double defaultTakeProfitPercentage;
    @Value("${bot.cycle.allowed}")
    private String defaultCycleAllowed;
    @Value("${bot.backtest.days}")
    private int defaultBacktestDays;
    @Value("${bot.backtest.initial.balance}")
    private double defaultBacktestInitialBalance;
    @Value("${bot.strategy.sma.period}")
    private int defaultSmaPeriod;
    @Value("${bot.strategy.ema.period}")
    private int defaultEmaPeriod;
    @Value("${bot.strategy.rsi.period}")
    private int defaultRsiPeriod;
    @Value("${bot.strategy.rsi.oversold}")
    private int defaultRsiOversold;
    @Value("${bot.strategy.rsi.overbought}")
    private int defaultRsiOverbought;
    @Value("${bot.strategy.macd.fast.period}")
    private int defaultMacdFastPeriod;
    @Value("${bot.strategy.macd.slow.period}")
    private int defaultMacdSlowPeriod;
    @Value("${bot.strategy.macd.signal.period}")
    private int defaultMacdSignalPeriod;
    @Value("${bot.strategy.composite.buy.threshold:0.5}")
    private double defaultCompositeBuyThreshold;
    @Value("${bot.strategy.composite.sell.threshold:-0.5}")
    private double defaultCompositeSellThreshold;

    private final JpaBotConfigRepository repository;

    // Runtime values
    private String strategyType;
    private int pollIntervalSeconds;
    private double initialBalance;
    private boolean trailingStopLossEnabled;
    private double trailingStopLossPercentage;
    private boolean takeProfitEnabled;
    private double takeProfitPercentage;
    private String cycleAllowed;
    private int backtestDays;
    private double backtestInitialBalance;
    private int smaPeriod;
    private int emaPeriod;
    private int rsiPeriod;
    private int rsiOversold;
    private int rsiOverbought;
    private int macdFastPeriod;
    private int macdSlowPeriod;
    private int macdSignalPeriod;
    private double compositeBuyThreshold;
    private double compositeSellThreshold;

    public ConfigurationService(JpaBotConfigRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initialize() {
        strategyType              = getString (KEY_STRATEGY_TYPE,            defaultStrategyType);
        pollIntervalSeconds       = getInt    (KEY_POLL_INTERVAL,            defaultPollIntervalSeconds);
        initialBalance            = getDouble (KEY_INITIAL_BALANCE,          defaultInitialBalance);
        trailingStopLossEnabled   = getBoolean(KEY_TRAILING_SL_ENABLED,     defaultTrailingStopLossEnabled);
        trailingStopLossPercentage= getDouble (KEY_TRAILING_SL_PERCENTAGE,  defaultTrailingStopLossPercentage);
        takeProfitEnabled         = getBoolean(KEY_TAKE_PROFIT_ENABLED,     defaultTakeProfitEnabled);
        takeProfitPercentage      = getDouble (KEY_TAKE_PROFIT_PERCENTAGE,  defaultTakeProfitPercentage);
        cycleAllowed              = getString (KEY_CYCLE_ALLOWED,           defaultCycleAllowed);
        backtestDays              = getInt    (KEY_BACKTEST_DAYS,           defaultBacktestDays);
        backtestInitialBalance    = getDouble (KEY_BACKTEST_INITIAL_BALANCE,defaultBacktestInitialBalance);
        smaPeriod                 = getInt    (KEY_SMA_PERIOD,              defaultSmaPeriod);
        emaPeriod                 = getInt    (KEY_EMA_PERIOD,              defaultEmaPeriod);
        rsiPeriod                 = getInt    (KEY_RSI_PERIOD,              defaultRsiPeriod);
        rsiOversold               = getInt    (KEY_RSI_OVERSOLD,            defaultRsiOversold);
        rsiOverbought             = getInt    (KEY_RSI_OVERBOUGHT,          defaultRsiOverbought);
        macdFastPeriod            = getInt    (KEY_MACD_FAST_PERIOD,        defaultMacdFastPeriod);
        macdSlowPeriod            = getInt    (KEY_MACD_SLOW_PERIOD,        defaultMacdSlowPeriod);
        macdSignalPeriod          = getInt    (KEY_MACD_SIGNAL_PERIOD,      defaultMacdSignalPeriod);
        compositeBuyThreshold     = getDouble (KEY_COMPOSITE_BUY_THRESHOLD, defaultCompositeBuyThreshold);
        compositeSellThreshold    = getDouble (KEY_COMPOSITE_SELL_THRESHOLD,defaultCompositeSellThreshold);
        log.info("Bot configuration loaded from DB (strategy={}, poll={}s, sma={}, ema={}, rsi={}, macd={}/{}/{})",
                strategyType, pollIntervalSeconds, smaPeriod, emaPeriod, rsiPeriod,
                macdFastPeriod, macdSlowPeriod, macdSignalPeriod);
    }

    // Getters
    public String  getStrategyType()              { return strategyType; }
    public int     getPollIntervalSeconds()        { return pollIntervalSeconds; }
    public double  getInitialBalance()             { return initialBalance; }
    public boolean isTrailingStopLossEnabled()     { return trailingStopLossEnabled; }
    public double  getTrailingStopLossPercentage() { return trailingStopLossPercentage; }
    public boolean isTakeProfitEnabled()           { return takeProfitEnabled; }
    public double  getTakeProfitPercentage()       { return takeProfitPercentage; }
    public String  getCycleAllowed()               { return cycleAllowed; }
    public int     getBacktestDays()               { return backtestDays; }
    public double  getBacktestInitialBalance()     { return backtestInitialBalance; }
    public int     getSmaPeriod()                  { return smaPeriod; }
    public int     getEmaPeriod()                  { return emaPeriod; }
    public int     getRsiPeriod()                  { return rsiPeriod; }
    public int     getRsiOversold()                { return rsiOversold; }
    public int     getRsiOverbought()              { return rsiOverbought; }
    public int     getMacdFastPeriod()             { return macdFastPeriod; }
    public int     getMacdSlowPeriod()             { return macdSlowPeriod; }
    public int     getMacdSignalPeriod()           { return macdSignalPeriod; }
    public double  getCompositeBuyThreshold()      { return compositeBuyThreshold; }
    public double  getCompositeSellThreshold()     { return compositeSellThreshold; }

    public synchronized void saveStrategyType(String type) {
        this.strategyType = type;
        persist(KEY_STRATEGY_TYPE, type);
    }

    public synchronized void savePollIntervalSeconds(int seconds) {
        this.pollIntervalSeconds = seconds;
        persist(KEY_POLL_INTERVAL, seconds);
    }

    public synchronized void updateConfiguration(
            int smaPeriod, int emaPeriod,
            int rsiPeriod, int rsiOversold, int rsiOverbought,
            int macdFastPeriod, int macdSlowPeriod, int macdSignalPeriod,
            double compositeBuyThreshold, double compositeSellThreshold
    ) {
        this.smaPeriod              = smaPeriod;
        this.emaPeriod              = emaPeriod;
        this.rsiPeriod              = rsiPeriod;
        this.rsiOversold            = rsiOversold;
        this.rsiOverbought          = rsiOverbought;
        this.macdFastPeriod         = macdFastPeriod;
        this.macdSlowPeriod         = macdSlowPeriod;
        this.macdSignalPeriod       = macdSignalPeriod;
        this.compositeBuyThreshold  = compositeBuyThreshold;
        this.compositeSellThreshold = compositeSellThreshold;

        persist(KEY_SMA_PERIOD,              smaPeriod);
        persist(KEY_EMA_PERIOD,              emaPeriod);
        persist(KEY_RSI_PERIOD,              rsiPeriod);
        persist(KEY_RSI_OVERSOLD,            rsiOversold);
        persist(KEY_RSI_OVERBOUGHT,          rsiOverbought);
        persist(KEY_MACD_FAST_PERIOD,        macdFastPeriod);
        persist(KEY_MACD_SLOW_PERIOD,        macdSlowPeriod);
        persist(KEY_MACD_SIGNAL_PERIOD,      macdSignalPeriod);
        persist(KEY_COMPOSITE_BUY_THRESHOLD, compositeBuyThreshold);
        persist(KEY_COMPOSITE_SELL_THRESHOLD,compositeSellThreshold);
        log.info("Bot strategy parameters saved to DB");
    }

    @SuppressWarnings("null")
    private String getString(String key, String defaultValue) {
        return repository.findById(key)
                .map(BotConfigEntity::getValue)
                .orElseGet(() -> { persist(key, defaultValue); return defaultValue; });
    }

    @SuppressWarnings("null")
    private int getInt(String key, int defaultValue) {
        return repository.findById(key)
                .map(e -> Integer.parseInt(e.getValue()))
                .orElseGet(() -> { persist(key, defaultValue); return defaultValue; });
    }

    @SuppressWarnings("null")
    private double getDouble(String key, double defaultValue) {
        return repository.findById(key)
                .map(e -> Double.parseDouble(e.getValue()))
                .orElseGet(() -> { persist(key, defaultValue); return defaultValue; });
    }

    @SuppressWarnings("null")
    private boolean getBoolean(String key, boolean defaultValue) {
        return repository.findById(key)
                .map(e -> Boolean.parseBoolean(e.getValue()))
                .orElseGet(() -> { persist(key, defaultValue); return defaultValue; });
    }

    @SuppressWarnings("null")
    private void persist(String key, Object value) {
        BotConfigEntity entity = repository.findById(key)
                .orElse(new BotConfigEntity(key, ""));
        entity.setValue(String.valueOf(value));
        repository.save(entity);
    }
}
