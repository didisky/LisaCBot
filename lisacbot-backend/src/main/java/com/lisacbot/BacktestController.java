package com.lisacbot;

import org.springframework.web.bind.annotation.*;

/**
 * REST controller for running backtests.
 */
@RestController
@RequestMapping("/api/backtest")
public class BacktestController {

    private final BacktestService backtestService;

    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    /**
     * Runs a backtest with default configuration.
     *
     * @return backtest results
     */
    @PostMapping
    public BacktestResult runBacktest() {
        return backtestService.runBacktest();
    }

    /**
     * Runs a backtest with custom parameters.
     *
     * @param days number of days of historical data
     * @param balance initial balance in USD
     * @return backtest results
     */
    @PostMapping("/custom")
    public BacktestResult runCustomBacktest(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "1000.0") double balance) {
        return backtestService.runBacktest(days, balance);
    }
}
