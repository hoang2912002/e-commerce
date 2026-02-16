package com.fashion.order.service;

import java.util.concurrent.CompletableFuture;

import com.fashion.order.dto.response.SagaStateResponse;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent;
import com.fashion.order.entity.Order;

public interface SagaStateService {
    CompletableFuture<SagaStateResponse> executeOrderSaga(OrderCreatedEvent orderCreatedEvent, Order order);
}
