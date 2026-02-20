package com.fashion.product.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fashion.product.dto.request.PromotionRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.PromotionResponse;
import com.fashion.product.dto.response.PromotionResponse.InnerPromotionResponse;
import com.fashion.product.entity.ProductSku;
import com.fashion.product.entity.Promotion;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionRequest promotion);
    PromotionResponse updatePromotion(PromotionRequest promotion);
    PromotionResponse getPromotionById(UUID id);
    PaginationResponse<List<PromotionResponse>> getAllPromotion(SearchRequest request);
    void spinningQuantity(Map<UUID, Integer> promotions, UUID eventId, Boolean isRestore);
    Promotion lockPromotionById(UUID id);
    InnerPromotionResponse getInternalCorrespondingPromotionByProductId(ProductSku productSku);
    Map<UUID, InnerPromotionResponse> getBestPromotionsByProductIds(Collection<ProductSku> productSkus,UUID productIds);
    Void warmPromotion();
    void updateQuantityPromotion(UUID id, Integer quantity); // Only use for schedule cronjob
}
