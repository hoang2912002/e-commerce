package com.fashion.product.dto.response.kafka;

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
public class ShopManagementAddressEvent {
    UUID id;
    UUID shopManagementId;
    String address;
    String district;
    String province;
    String ward;
}
