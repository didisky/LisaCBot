package com.lisacbot.domain.port;

import com.lisacbot.domain.model.Trade;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Port interface for trade persistence.
 */
public interface TradeRepository {
    /**
     * Saves a trade to the repository.
     *
     * @param trade the trade to save
     * @return the saved trade with generated ID
     */
    Trade save(Trade trade);

    /**
     * Finds all trades ordered by timestamp descending.
     *
     * @return list of all trades
     */
    List<Trade> findAll();

    /**
     * Finds trades within a date range.
     *
     * @param start start date
     * @param end end date
     * @return list of trades in the range
     */
    List<Trade> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
