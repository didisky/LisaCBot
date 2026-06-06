package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.port.PriceHistoryRepository;
import com.lisacbot.domain.service.PriceEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceHistoryController {

    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceEventPublisher priceEventPublisher;

    public PriceHistoryController(PriceHistoryRepository priceHistoryRepository, PriceEventPublisher priceEventPublisher) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceEventPublisher = priceEventPublisher;
    }

    @GetMapping("/range")
    public List<Price> getPricesByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        ZoneId systemZone = ZoneId.systemDefault();
        LocalDateTime localStart = start.atZone(ZoneOffset.UTC).withZoneSameInstant(systemZone).toLocalDateTime();
        LocalDateTime localEnd = end.atZone(ZoneOffset.UTC).withZoneSameInstant(systemZone).toLocalDateTime();
        return priceHistoryRepository.findByTimestampBetween(localStart, localEnd);
    }

    @GetMapping("/latest")
    public List<Price> getLatestPrices(
            @RequestParam(defaultValue = "100") int limit
    ) {
        return priceHistoryRepository.findLatest(limit);
    }

    @GetMapping("/events")
    public SseEmitter streamPriceEvents() {
        return priceEventPublisher.createEmitter();
    }
}
