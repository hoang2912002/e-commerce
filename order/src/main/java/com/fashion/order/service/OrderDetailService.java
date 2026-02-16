package com.fashion.order.service;

import java.util.List;

import com.fashion.order.entity.Order;
import com.fashion.order.entity.OrderDetail;

public interface OrderDetailService {
    Void saveAllOrderDetail(List<OrderDetail> orderDetails, Order order);
}
