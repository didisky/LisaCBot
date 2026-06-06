package com.lisacbot.infrastructure.persistence;

import com.lisacbot.domain.model.Price;
import com.lisacbot.domain.port.PriceHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PriceHistoryRepositoryAdapter implements PriceHistoryRepository {

    private final JpaPriceHistoryRepository jpaRepository;

    public PriceHistoryRepositoryAdapter(JpaPriceHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Price price) {
        jpaRepository.save(PriceHistoryEntity.fromDomain(price));
    }

    private static final int MAX_CHART_POINTS = 500;

    @Override
    public List<Price> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        List<PriceHistoryEntity> all = jpaRepository.findByTimestampBetweenOrderByTimestampAsc(start, end);
        if (all.size() <= MAX_CHART_POINTS) {
            return all.stream().map(PriceHistoryEntity::toDomain).toList();
        }
        int step = all.size() / MAX_CHART_POINTS;
        List<Price> result = new ArrayList<>(MAX_CHART_POINTS);
        for (int i = 0; i < all.size(); i += step) {
            result.add(all.get(i).toDomain());
        }
        return result;
    }

    @Override
    public List<Price> findLatest(int limit) {
        return jpaRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit))
                .stream()
                .map(PriceHistoryEntity::toDomain)
                .toList();
    }
}
