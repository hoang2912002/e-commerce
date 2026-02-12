package com.fashion.inventory.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.annotation.MergedAnnotations.Search;

import com.fashion.inventory.dto.request.InventoryRequest;
import com.fashion.inventory.dto.request.InventoryRequest.BaseInventoryRequest;
import com.fashion.inventory.dto.request.InventoryRequest.InnerOrderDetail_FromOrderRequest;
import com.fashion.inventory.dto.request.InventoryRequest.ReturnAvailableQuantity;
import com.fashion.inventory.dto.request.search.SearchRequest;
import com.fashion.inventory.dto.response.InventoryResponse;
import com.fashion.inventory.dto.response.PaginationResponse;
import com.fashion.inventory.dto.response.kafka.OrderCreatedEvent;
import com.fashion.inventory.dto.response.kafka.ProductApprovedEvent;
import com.fashion.inventory.entity.Inventory;

public interface InventoryService {
    List<InventoryResponse> findRawListInventoryBySku(List<UUID> skuIds);
    // Internal use in order-service, product-service
    boolean existsByProductSkuId(UUID id);
    // Internal use in product-service
    List<UUID> adjustmentsStockAfterProductApproved(List<ProductApprovedEvent> inventories, UUID eventId);

    InventoryResponse createInventory(InventoryRequest inventory);
    InventoryResponse updateInventory(InventoryRequest inventory);
    InventoryResponse getInventoryById(UUID id, Long version);
    PaginationResponse<List<InventoryResponse>> getAllInventories(SearchRequest request);
    // Internal use in product-service
    List<Inventory> findRawInventoriesByProductId(UUID productId);
    // Internal use in order-service
    Inventory findRawInventoryByProductIdAndProductSkuId(UUID productId, UUID productSkuId);
    void deleteInventoryById(UUID id);
    // Internal use in order-service
    void changeQuantityUse(
        Collection<ReturnAvailableQuantity> requests,
        UUID eventId
    );
    void checkInternalQuantityAvailableForOrder(Collection<InnerOrderDetail_FromOrderRequest> inventory);
}
