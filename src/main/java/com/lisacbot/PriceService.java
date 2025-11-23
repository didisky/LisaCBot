package com.lisacbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class PriceService {
    private static final Logger log = LoggerFactory.getLogger(PriceService.class);

    private final RestClient restClient;
    private final String apiUrl;

    public PriceService(@Value("${bot.price.api.url}") String apiUrl) {
        this.restClient = RestClient.create();
        this.apiUrl = apiUrl;
    }

    /**
     * Fetches current Bitcoin price from CoinGecko API.
     *
     * @return BTC price in USD
     * @throws RuntimeException if API call fails or response is invalid
     */
    @SuppressWarnings("unchecked")
    public double getBitcoinPrice() {
        log.info("Fetching BTC price from CoinGecko");

        Map<String, Map<String, Number>> response = restClient.get()
                .uri(apiUrl)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("bitcoin")) {
            throw new RuntimeException("Could not fetch Bitcoin price");
        }

        return response.get("bitcoin").get("usd").doubleValue();
    }
}
