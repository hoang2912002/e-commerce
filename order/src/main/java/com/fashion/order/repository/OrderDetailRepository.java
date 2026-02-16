package com.fashion.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fashion.order.entity.OrderDetail;
import com.fashion.order.entity.OrderDetailId;
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId>{
    
}
