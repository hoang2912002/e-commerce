package com.fashion.order.dto.response.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.fashion.order.dto.response.internal.PaymentResponse;
import com.fashion.order.dto.response.internal.PromotionResponse;
import com.fashion.order.dto.response.internal.ShippingResponse;
import com.fashion.order.dto.response.internal.InventoryResponse.ReturnAvailableQuantity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreatedEvent {
    Collection<ReturnAvailableQuantity> inventories;
    ShippingResponse shipping;
    PaymentResponse payment;
    Map<UUID, Integer> promotions;

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InternalOrderCreatedEvent extends ApplicationEvent{
        OrderCreatedEvent orderData;
        public InternalOrderCreatedEvent(Object source, OrderCreatedEvent orderData){
            super(source);
            this.orderData= orderData;
        }
        
    }
}
