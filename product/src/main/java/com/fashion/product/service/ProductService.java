package com.fashion.product.service;

import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.request.VariantRequest.InnerVariantRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.entity.Product;

public interface ProductService {
    ProductResponse createProduct(Product product, List<InnerVariantRequest> variants);
    ProductResponse updateProduct(Product product, List<InnerVariantRequest> variants);
    PaginationResponse getAllProduct();
    ProductResponse getProductById(UUID id);
    List<Product> findListProductById(List<UUID> ids);
    Product lockProductById(UUID id);
    Product findRawProductById(UUID id);
}
