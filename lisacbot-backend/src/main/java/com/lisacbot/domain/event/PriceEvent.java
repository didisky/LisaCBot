package com.lisacbot.domain.event;

import com.lisacbot.domain.model.Price;
import org.springframework.context.ApplicationEvent;

public class PriceEvent extends ApplicationEvent {
    private final Price price;

    public PriceEvent(Object source, Price price) {
        super(source);
        this.price = price;
    }

    public Price getPrice() {
        return price;
    }
}
