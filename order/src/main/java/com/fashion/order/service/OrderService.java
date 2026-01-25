package com.fashion.order.service;

import java.util.List;
import java.util.UUID;

import com.fashion.order.common.enums.OrderEnum;
import com.fashion.order.dto.request.OrderRequest;
import com.fashion.order.dto.request.OrderDetailRequest.InnerOrderDetail_FromOrderRequest;
import com.fashion.order.dto.request.search.SearchRequest;
import com.fashion.order.dto.response.OrderResponse;
import com.fashion.order.dto.response.PaginationResponse;

public interface OrderService {
    OrderResponse createOrder(OrderRequest order);
    OrderResponse updateOrder(OrderRequest order);
    OrderResponse updateOrderStatus(UUID id, OrderEnum status, String note);
    OrderResponse getOrderById(UUID id);
    OrderResponse getOrderByCode(String code);
    PaginationResponse<List<OrderResponse>> getAllOrder(SearchRequest request);
    void deleteOrderById(UUID id);
}
