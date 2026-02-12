package com.fashion.inventory.messaging.provider;

import java.util.List;
import java.util.UUID;

import com.fashion.inventory.dto.response.kafka.EventMetaData;

public interface InventoryServiceProvider {
    void produceInventoryCreatedSuccess(List<UUID> productSkuIds, EventMetaData metadata);
    void produceInventoryCreationFailed(UUID productSkuIds);
}
