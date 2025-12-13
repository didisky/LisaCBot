package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for bot control operations (start/stop).
 */
@RestController
@RequestMapping("/api/bot")
@CrossOrigin(origins = "http://localhost:4200")
public class BotController {

    private final TradingService tradingService;

    public BotController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Start the trading bot.
     *
     * @return response with success message
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startBot() {
        tradingService.start();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bot started successfully",
                "status", tradingService.getBotStatus()
        ));
    }

    /**
     * Stop the trading bot.
     *
     * @return response with success message
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopBot() {
        tradingService.stop();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bot stopped successfully",
                "status", tradingService.getBotStatus()
        ));
    }
}
