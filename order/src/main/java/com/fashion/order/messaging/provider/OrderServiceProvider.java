package com.fashion.order.messaging.provider;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fashion.order.dto.response.SagaStateResponse;
import com.fashion.order.dto.response.internal.InventoryResponse.ReturnAvailableQuantity;
import com.fashion.order.dto.response.internal.PaymentResponse.InnerInternalPayment;
import com.fashion.order.dto.response.internal.ShippingResponse;
import com.fashion.order.entity.SagaState;

public interface OrderServiceProvider {
    CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessPayment(SagaState sagaState,InnerInternalPayment event);
    CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessPromotion(SagaState sagaState,Map<UUID, Integer> promotions);
    CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessShipping(SagaState sagaState,ShippingResponse shipping);
    CompletableFuture<SagaStateResponse> produceOrderCreatedEventSuccessInventory(SagaState sagaState,Collection<ReturnAvailableQuantity> inventories);
    CompletableFuture<Void> produceOrderCreatedEventSuccessPaymentFailed(SagaState sagaState,InnerInternalPayment event);
    CompletableFuture<Void> produceOrderCreatedEventSuccessPromotionFailed(SagaState sagaState,Map<UUID, Integer> promotions);
    CompletableFuture<Void> produceOrderCreatedEventSuccessShippingFailed(SagaState sagaState,ShippingResponse shipping);
    CompletableFuture<Void> produceOrderCreatedEventSuccessInventoryFailed(SagaState sagaState,Collection<ReturnAvailableQuantity> inventories);

}
