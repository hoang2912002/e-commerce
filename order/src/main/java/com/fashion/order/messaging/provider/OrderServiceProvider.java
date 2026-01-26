package com.fashion.order.messaging.provider;

import com.fashion.order.dto.response.kafka.OrderCreatedEvent.InternalOrderCreatedEvent;

public interface OrderServiceProvider {
    void produceOrderCreatedEventSuccess(InternalOrderCreatedEvent event);
}
