package com.lisacbot;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving bot status.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class StatusController {

    private final Bot bot;

    public StatusController(Bot bot) {
        this.bot = bot;
    }

    /**
     * Get current bot status including balance, holdings, and Bitcoin price.
     *
     * @return BotStatus object with current state
     */
    @GetMapping("/status")
    public BotStatus getStatus() {
        return bot.getBotStatus();
    }
}
