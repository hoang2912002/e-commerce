package com.fashion.shipping.dto.response.kafka;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.fashion.shipping.dto.response.ShippingResponse;
import com.fashion.shipping.dto.response.internal.PaymentResponse;
import com.fashion.shipping.dto.response.internal.PaymentResponse.InnerInternalPayment;
import com.fashion.shipping.dto.response.internal.InventoryResponse.ReturnAvailableQuantity;

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
    InnerInternalPayment payment;
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