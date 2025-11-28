package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.model.BacktestResult;
import com.lisacbot.domain.service.BacktestService;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for backtest endpoints.
 */
@RestController
@RequestMapping("/api/backtest")
@CrossOrigin(origins = "http://localhost:4200")
public class BacktestController {

    private final BacktestService backtestService;

    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    @PostMapping
    public BacktestResult runBacktest() {
        return backtestService.runBacktest();
    }

    @PostMapping("/custom")
    public BacktestResult runCustomBacktest(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "1000.0") double balance) {
        return backtestService.runBacktest(days, balance);
    }
}
