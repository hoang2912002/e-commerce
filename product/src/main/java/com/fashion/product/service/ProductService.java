package com.fashion.product.service;

import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.request.ProductRequest;
import com.fashion.product.dto.request.VariantRequest.InnerVariantRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.entity.Product;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(ProductRequest request);
    PaginationResponse<List<ProductResponse>> getAllProduct(SearchRequest request);
    ProductResponse getProductById(UUID id);
    List<Product> findListProductById(List<UUID> ids);
    void validateInternalProductById(UUID id, UUID skuId);
}
