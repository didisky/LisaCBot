package com.lisacbot.infrastructure.price;

import com.lisacbot.domain.port.PriceProvider;
import com.lisacbot.domain.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * CoinGecko implementation of PriceProvider.
 * Historical prices are served from the local CSV via CsvPriceAdapter.
 */
@Component
public class CoinGeckoPriceAdapter implements PriceProvider {
    private static final Logger log = LoggerFactory.getLogger(CoinGeckoPriceAdapter.class);

    private final RestClient restClient;
    private final String currentPriceApiUrl;
    private final CsvPriceAdapter csvPriceAdapter;

    public CoinGeckoPriceAdapter(
            @Value("${bot.price.api.url}") String currentPriceApiUrl,
            CsvPriceAdapter csvPriceAdapter
    ) {
        this.restClient = RestClient.create();
        this.currentPriceApiUrl = currentPriceApiUrl;
        this.csvPriceAdapter = csvPriceAdapter;
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
    public List<Price> getHistoricalPrices(int days) {
        return csvPriceAdapter.getHistoricalPrices(days);
    }
}
