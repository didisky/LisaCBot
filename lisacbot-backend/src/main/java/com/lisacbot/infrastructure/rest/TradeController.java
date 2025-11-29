package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.model.Trade;
import com.lisacbot.domain.model.TradeMetrics;
import com.lisacbot.domain.port.TradeRepository;
import com.lisacbot.domain.service.MetricsService;
import com.lisacbot.domain.service.TradeEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    private final MetricsService metricsService;
    private final TradeEventPublisher tradeEventPublisher;

    public TradeController(TradeRepository tradeRepository, MetricsService metricsService, TradeEventPublisher tradeEventPublisher) {
        this.tradeRepository = tradeRepository;
        this.metricsService = metricsService;
        this.tradeEventPublisher = tradeEventPublisher;
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

    /**
     * Get comprehensive trading performance metrics.
     *
     * @return calculated metrics including win rate, profit/loss, best/worst trades, etc.
     */
    @GetMapping("/metrics")
    public TradeMetrics getMetrics() {
        return metricsService.calculateMetrics();
    }

    /**
     * Server-Sent Events endpoint for real-time trade notifications.
     * Clients can subscribe to this endpoint to receive trade events as they happen.
     *
     * @return SseEmitter for streaming trade events
     */
    @GetMapping("/events")
    public SseEmitter streamTradeEvents() {
        return tradeEventPublisher.createEmitter();
    }
}
