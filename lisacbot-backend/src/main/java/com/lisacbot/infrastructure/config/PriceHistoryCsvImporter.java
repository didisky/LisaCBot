package com.lisacbot.infrastructure.config;

import com.lisacbot.domain.model.Price;
import com.lisacbot.infrastructure.persistence.JpaPriceHistoryRepository;
import com.lisacbot.infrastructure.persistence.PriceHistoryEntity;
import com.lisacbot.infrastructure.price.CsvPriceAdapter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Imports the full CSV price history into the price_history table on first startup.
 * Skipped automatically if the table already contains data.
 */
@Component
public class PriceHistoryCsvImporter {

    private static final Logger log = LoggerFactory.getLogger(PriceHistoryCsvImporter.class);
    private static final int BATCH_SIZE = 1000;

    private final CsvPriceAdapter csvPriceAdapter;
    private final JpaPriceHistoryRepository repository;

    public PriceHistoryCsvImporter(CsvPriceAdapter csvPriceAdapter, JpaPriceHistoryRepository repository) {
        this.csvPriceAdapter = csvPriceAdapter;
        this.repository = repository;
    }

    @SuppressWarnings("null")
    @PostConstruct
    public void importIfEmpty() {
        // Skip only if the table already contains historical data (more than just live bot recordings).
        // We consider the CSV imported when the oldest record predates 2021 (CSV starts in 2020).
        java.time.LocalDateTime threshold = java.time.LocalDateTime.of(2021, 1, 1, 0, 0);
        boolean hasHistory = repository.findByTimestampBetweenOrderByTimestampAsc(
                java.time.LocalDateTime.of(2020, 1, 1, 0, 0), threshold).size() > 0;
        if (hasHistory) {
            log.info("price_history already contains historical CSV data — import skipped");
            return;
        }

        List<Price> all = csvPriceAdapter.getAllPrices();
        if (all.isEmpty()) {
            log.warn("CSV price data is empty — nothing to import");
            return;
        }

        log.info("Importing {} historical price records from CSV into price_history...", all.size());
        long start = System.currentTimeMillis();
        int imported = 0;

        for (int i = 0; i < all.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, all.size());
            ArrayList<PriceHistoryEntity> batch = new ArrayList<>(end - i);
            for (int j = i; j < end; j++) {
                batch.add(PriceHistoryEntity.fromDomain(all.get(j)));
            }
            repository.saveAll(batch);
            imported += batch.size();
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("CSV import complete: {} records imported in {}ms", imported, elapsed);
    }
}
