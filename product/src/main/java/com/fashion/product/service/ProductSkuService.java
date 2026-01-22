package com.fashion.product.service;

import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.response.ProductSkuResponse;
import com.fashion.product.entity.Product;

public interface ProductSkuService {
    void deleteProductSkuByListId(List<Long> ids);
    void validateAndMapSkuToInventoryRequests(Product product);
    List<ProductSkuResponse> getInternalProductSkuByIds(List<UUID> ids);
}
