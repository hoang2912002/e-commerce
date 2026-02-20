package com.fashion.shipping.service;

import java.util.List;
import java.util.UUID;

import com.fashion.shipping.dto.request.ShippingRequest;
import com.fashion.shipping.dto.request.search.SearchRequest;
import com.fashion.shipping.dto.response.PaginationResponse;
import com.fashion.shipping.dto.response.ShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerInternalShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerTempShippingFeeResponse;
import com.fashion.shipping.dto.response.internal.PaymentResponse;
import com.fashion.shipping.dto.response.kafka.SagaStateResponse;

public interface ShippingService {
    ShippingResponse getShippingById(UUID id, String date, Long version);
    PaginationResponse<List<ShippingResponse>> getAllShipping(SearchRequest request);
    ShippingResponse createShipping(ShippingRequest shippingRequest);
    ShippingResponse updateShipping(ShippingRequest shippingRequest);
    SagaStateResponse commandShipping(ShippingResponse shippingResponse, UUID eventId); // From order saga orchestration
    SagaStateResponse compensateShipping(ShippingResponse shippingResponse, UUID eventId); // From order saga orchestration
    InnerTempShippingFeeResponse getThirdPartyShippingFee(InnerInternalShippingResponse request);
}
