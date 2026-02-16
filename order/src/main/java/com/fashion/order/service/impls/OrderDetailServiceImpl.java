package com.fashion.order.service.impls;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fashion.order.common.enums.EnumError;
import com.fashion.order.entity.Order;
import com.fashion.order.entity.OrderDetail;
import com.fashion.order.exception.ServiceException;
import com.fashion.order.repository.OrderDetailRepository;
import com.fashion.order.service.OrderDetailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailServiceImpl implements OrderDetailService{
    OrderDetailRepository orderDetailRepository;

    @Override
    public Void saveAllOrderDetail(List<OrderDetail> orderDetails, Order order) {
        try {
            List<OrderDetail> savOrderDetail = orderDetails.stream().map(
                o -> {
                    o.setOrder(order);
                    o.setOrderId(order.getId());
                    o.setOrderCreatedAt(order.getCreatedAt());
                    return o;
                }
            ).distinct().toList();
            this.orderDetailRepository.saveAll(savOrderDetail);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [saveAllOrderDetail] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
        return null;
    }

    
}
