package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.port.PriceHistoryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceHistoryController {

    private final PriceHistoryRepository priceHistoryRepository;

    public PriceHistoryController(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
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
}
