package com.lisacbot.domain.event;

import com.lisacbot.domain.model.Trade;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new trade is executed.
 */
public class TradeEvent extends ApplicationEvent {
    private final Trade trade;

    public TradeEvent(Object source, Trade trade) {
        super(source);
        this.trade = trade;
    }

    public Trade getTrade() {
        return trade;
    }
}
