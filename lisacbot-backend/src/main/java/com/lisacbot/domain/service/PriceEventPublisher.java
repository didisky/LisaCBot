package com.lisacbot.domain.service;

import com.lisacbot.domain.event.PriceEvent;
import com.lisacbot.domain.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PriceEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PriceEventPublisher.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ApplicationEventPublisher eventPublisher;

    public PriceEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishPriceEvent(Price price) {
        eventPublisher.publishEvent(new PriceEvent(this, price));
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        emitters.add(emitter);
        log.info("New price SSE connection registered. Total: {}", emitters.size());

        return emitter;
    }

    @EventListener
    public void handlePriceEvent(PriceEvent event) {
        Price price = event.getPrice();
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("price").data(price));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }
}
