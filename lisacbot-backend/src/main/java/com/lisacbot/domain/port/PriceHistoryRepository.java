package com.lisacbot.domain.port;

import com.lisacbot.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceHistoryRepository {

    void save(Price price);

    List<Price> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<Price> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<Price> findLatest(int limit);
}
