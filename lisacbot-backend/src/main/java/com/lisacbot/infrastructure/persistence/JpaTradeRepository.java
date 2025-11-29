package com.lisacbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for TradeEntity.
 */
@Repository
public interface JpaTradeRepository extends JpaRepository<TradeEntity, Long> {
    List<TradeEntity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    List<TradeEntity> findAllByOrderByTimestampDesc();
}
