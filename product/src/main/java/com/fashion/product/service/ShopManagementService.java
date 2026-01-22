package com.fashion.product.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fashion.product.dto.request.ShopManagementRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ShopManagementResponse;
import com.fashion.product.entity.ShopManagement;

public interface ShopManagementService {
    ShopManagementResponse createShopManagement(ShopManagementRequest shopManagement);
    ShopManagementResponse updateShopManagement(ShopManagementRequest shopManagement);
    ShopManagementResponse getShopManagementById(UUID id);
    PaginationResponse<List<ShopManagementResponse>> getAllShopManagement(SearchRequest request);
    void deleteShopManagementById(UUID id);
    Map<String, Object[]> detectChangedFields(ShopManagement oldData, ShopManagement newData);
}
