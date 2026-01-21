package com.fashion.product.dto.response.kafka;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

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
public class ProductApprovedEvent {
    UUID id;
    Integer quantityAvailable;
    Integer quantityReserved;
    Integer quantitySold;
    UUID productId;
    UUID productSkuId;

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InternalProductApprovedEvent extends ApplicationEvent {
        List<ProductApprovedEvent> inventoriesData;

        public InternalProductApprovedEvent(Object source, List<ProductApprovedEvent> inventoriesData) {
            super(source);
            this.inventoriesData = inventoriesData;
        }
    }
}
