package com.lisacbot.price;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service to fetch historical Bitcoin price data from CoinGecko API.
 */
@Service
public class HistoricalPriceService {
    private static final Logger log = LoggerFactory.getLogger(HistoricalPriceService.class);

    private final RestClient restClient;
    private final String historicalApiUrl;

    public HistoricalPriceService(@Value("${bot.backtest.api.url}") String historicalApiUrl) {
        this.restClient = RestClient.create();
        this.historicalApiUrl = historicalApiUrl;
    }

    /**
     * Fetches historical Bitcoin prices for the specified number of days.
     *
     * @param days number of days of historical data to fetch
     * @return list of price data points
     * @throws RuntimeException if API call fails or response is invalid
     */
    @SuppressWarnings("unchecked")
    public List<PriceData> getHistoricalPrices(int days) {
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
        List<PriceData> result = new ArrayList<>();

        for (List<Number> dataPoint : pricesData) {
            long timestamp = dataPoint.get(0).longValue();
            double price = dataPoint.get(1).doubleValue();
            result.add(new PriceData(timestamp, price));
        }

        log.info("Fetched {} historical price points", result.size());
        return result;
    }
}
