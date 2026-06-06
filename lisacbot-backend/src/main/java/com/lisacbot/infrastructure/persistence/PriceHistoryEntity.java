package com.lisacbot.infrastructure.persistence;

import com.lisacbot.domain.model.Price;
import jakarta.persistence.*;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_history", indexes = {
        @Index(name = "idx_price_history_timestamp", columnList = "timestamp")
})
public class PriceHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private double price;

    protected PriceHistoryEntity() {
    }

    public PriceHistoryEntity(LocalDateTime timestamp, double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    @NonNull
    public static PriceHistoryEntity fromDomain(Price price) {
        return new PriceHistoryEntity(price.timestamp(), price.value());
    }

    public Price toDomain() {
        return new Price(price, timestamp);
    }

    public Long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getPrice() { return price; }
}
