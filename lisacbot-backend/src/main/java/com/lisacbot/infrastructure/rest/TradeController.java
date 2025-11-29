package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.model.Trade;
import com.lisacbot.domain.port.TradeRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for trade history endpoints.
 */
@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "http://localhost:4200")
public class TradeController {

    private final TradeRepository tradeRepository;

    public TradeController(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    /**
     * Get all trades ordered by timestamp descending (most recent first).
     *
     * @return list of all trades
     */
    @GetMapping
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    /**
     * Get trades within a date range.
     *
     * @param start start date (ISO format: 2024-01-01T00:00:00)
     * @param end end date (ISO format: 2024-12-31T23:59:59)
     * @return list of trades in the range
     */
    @GetMapping("/range")
    public List<Trade> getTradesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return tradeRepository.findByTimestampBetween(start, end);
    }
}
