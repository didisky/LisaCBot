package com.lisacbot.domain.port;

import com.lisacbot.domain.model.Price;

import java.util.List;

/**
 * Interface for fetching cryptocurrency prices.
 */
public interface PriceProvider {
    Price getCurrentPrice();
    List<Price> getHistoricalPrices(int days);
}
