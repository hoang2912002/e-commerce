package com.fashion.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order,UUID>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByCode(String code);
}
