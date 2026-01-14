package com.fashion.product.service;

import java.util.List;

import com.fashion.product.entity.Product;

public interface ProductSkuService {
    void deleteProductSkuByListId(List<Long> ids);
    void validateAndMapSkuToInventoryRequests(Product product);
}
