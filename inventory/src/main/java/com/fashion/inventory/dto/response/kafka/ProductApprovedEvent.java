package com.fashion.inventory.dto.response.kafka;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
}
