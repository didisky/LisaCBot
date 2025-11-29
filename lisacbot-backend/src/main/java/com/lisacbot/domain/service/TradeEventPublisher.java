package com.lisacbot.domain.service;

import com.lisacbot.domain.event.TradeEvent;
import com.lisacbot.domain.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service responsible for managing SSE connections and broadcasting trade events.
 */
@Service
public class TradeEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(TradeEventPublisher.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ApplicationEventPublisher eventPublisher;

    public TradeEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes a trade event to the application event bus.
     *
     * @param trade the trade to publish
     */
    public void publishTradeEvent(Trade trade) {
        log.info("Publishing trade event: {}", trade);
        eventPublisher.publishEvent(new TradeEvent(this, trade));
    }

    /**
     * Registers a new SSE emitter for trade events.
     *
     * @return the created SseEmitter
     */
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L); // No timeout

        emitter.onCompletion(() -> {
            log.info("SSE connection completed");
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout");
            emitters.remove(emitter);
        });

        emitter.onError((e) -> {
            log.error("SSE connection error: {}", e.getMessage());
            emitters.remove(emitter);
        });

        emitters.add(emitter);
        log.info("New SSE connection registered. Total connections: {}", emitters.size());

        return emitter;
    }

    /**
     * Listens for TradeEvent and broadcasts to all connected SSE clients.
     *
     * @param event the trade event
     */
    @EventListener
    public void handleTradeEvent(TradeEvent event) {
        Trade trade = event.getTrade();
        log.info("Broadcasting trade event to {} clients: {}", emitters.size(), trade);

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("trade")
                        .data(trade));
            } catch (IOException e) {
                log.error("Failed to send event to client: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        // Clean up dead connections
        emitters.removeAll(deadEmitters);
    }
}
