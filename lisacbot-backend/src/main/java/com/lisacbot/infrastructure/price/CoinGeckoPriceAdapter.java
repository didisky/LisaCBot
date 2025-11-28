package com.lisacbot.infrastructure.price;

import com.lisacbot.domain.port.PriceProvider;
import com.lisacbot.domain.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CoinGecko implementation of PriceProvider.
 */
@Component
public class CoinGeckoPriceAdapter implements PriceProvider {
    private static final Logger log = LoggerFactory.getLogger(CoinGeckoPriceAdapter.class);

    private final RestClient restClient;
    private final String currentPriceApiUrl;
    private final String historicalApiUrl;

    public CoinGeckoPriceAdapter(
            @Value("${bot.price.api.url}") String currentPriceApiUrl,
            @Value("${bot.backtest.api.url}") String historicalApiUrl
    ) {
        this.restClient = RestClient.create();
        this.currentPriceApiUrl = currentPriceApiUrl;
        this.historicalApiUrl = historicalApiUrl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Price getCurrentPrice() {
        log.info("Fetching BTC price from CoinGecko");

        Map<String, Map<String, Number>> response = restClient.get()
                .uri(currentPriceApiUrl)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("bitcoin")) {
            throw new RuntimeException("Could not fetch Bitcoin price");
        }

        double price = response.get("bitcoin").get("usd").doubleValue();
        return new Price(price);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Price> getHistoricalPrices(int days) {
        log.info("Fetching {} days of historical BTC price data", days);

        String url = historicalApiUrl + "?vs_currency=usd&days=" + days;

        Map<String, List<List<Number>>> response = restClient.get()
                .uri(url)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("prices")) {
            throw new RuntimeException("Could not fetch historical Bitcoin prices");
        }

        List<List<Number>> pricesData = response.get("prices");
        List<Price> result = new ArrayList<>();

        for (List<Number> dataPoint : pricesData) {
            long timestampMillis = dataPoint.get(0).longValue();
            double price = dataPoint.get(1).doubleValue();

            LocalDateTime timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestampMillis),
                    ZoneId.systemDefault()
            );

            result.add(new Price(price, timestamp));
        }

        log.info("Fetched {} historical price points", result.size());
        return result;
    }
}
