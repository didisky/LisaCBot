package com.lisacbot.infrastructure.persistence;

import com.lisacbot.domain.model.Trade;
import com.lisacbot.domain.port.TradeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that bridges the domain TradeRepository port with JPA implementation.
 */
@Component
public class TradeRepositoryAdapter implements TradeRepository {

    private final JpaTradeRepository jpaRepository;

    public TradeRepositoryAdapter(JpaTradeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Trade save(Trade trade) {
        TradeEntity entity = TradeEntity.fromDomain(trade);
        TradeEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<Trade> findAll() {
        return jpaRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(TradeEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Trade> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByTimestampBetweenOrderByTimestampDesc(start, end)
                .stream()
                .map(TradeEntity::toDomain)
                .collect(Collectors.toList());
    }
}
