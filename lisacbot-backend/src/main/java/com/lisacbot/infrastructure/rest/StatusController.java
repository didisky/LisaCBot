package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.model.BotStatus;
import com.lisacbot.domain.service.TradingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for bot status endpoints.
 */
@RestController
@RequestMapping("/api")
public class StatusController {

    private final TradingService tradingService;

    public StatusController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @GetMapping("/status")
    public BotStatus getStatus() {
        return tradingService.getBotStatus();
    }
}
