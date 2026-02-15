package com.lisacbot.infrastructure.price;

import com.lisacbot.domain.model.Price;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads historical BTC prices from a local CSV file at startup.
 * CSV format: timestamp,datetime,open,high,low,close,volume_btc,volume_usd
 */
@Component
public class CsvPriceAdapter {

    private static final Logger log = LoggerFactory.getLogger(CsvPriceAdapter.class);
    private static final String CSV_FILE = "bitcoin_hourly_2020_2025.csv";

    private List<Price> allPrices = Collections.emptyList();

    @PostConstruct
    public void loadCsv() {
        log.info("Loading historical BTC prices from {}", CSV_FILE);
        List<Price> prices = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(CSV_FILE).getInputStream()))) {

            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                long timestampSeconds = Long.parseLong(parts[0].trim());
                double close = Double.parseDouble(parts[5].trim());

                LocalDateTime timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestampSeconds),
                        ZoneId.systemDefault()
                );
                prices.add(new Price(close, timestamp));
            }

            allPrices = Collections.unmodifiableList(prices);
            log.info("Loaded {} historical price points from CSV (from {} to {})",
                    allPrices.size(),
                    allPrices.get(0).timestamp(),
                    allPrices.get(allPrices.size() - 1).timestamp());

        } catch (Exception e) {
            log.error("Failed to load CSV historical prices: {}", e.getMessage(), e);
            throw new RuntimeException("Could not load historical price data from CSV", e);
        }
    }

    /**
     * Returns the last {@code days} days of historical prices from the CSV.
     */
    public List<Price> getHistoricalPrices(int days) {
        if (allPrices.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime cutoff = allPrices.get(allPrices.size() - 1).timestamp().minusDays(days);
        List<Price> result = new ArrayList<>();
        for (Price p : allPrices) {
            if (!p.timestamp().isBefore(cutoff)) {
                result.add(p);
            }
        }

        log.info("Returning {} price points for {} days from CSV", result.size(), days);
        return result;
    }
}
