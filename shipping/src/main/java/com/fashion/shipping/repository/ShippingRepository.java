package com.fashion.shipping.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.shipping.entity.Shipping;
import com.fashion.shipping.entity.ShippingId;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, ShippingId>, JpaSpecificationExecutor<Shipping>{
    @Query("SELECT s FROM Shipping s WHERE s.id = :id AND s.createdAt >= :start AND s.createdAt < :end")
    Optional<Shipping> findByIdInPartition(
        @Param("id") UUID id,
        @Param("start") Instant start,
        @Param("end") Instant end 
    );

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Shipping s WHERE s.id = :id " +
           "AND s.createdAt >= :start AND s.createdAt < :end")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<Shipping> lockShippingById(
        @Param("id") UUID id,
        @Param("start") Instant start,
        @Param("end") Instant end
    );

    Boolean existsByEventId(UUID eventId);

    @Query("SELECT s FROM Shipping s WHERE s.orderId = :orderId " +
           "AND s.createdAt >= :start AND s.createdAt < :end")
    Optional<Shipping> findDuplicateForCreate(
        @Param("orderId") UUID orderId,
        @Param("start") Instant start,
        @Param("end") Instant end
    );
    
    @Query("SELECT s FROM Shipping s WHERE s.orderId = :orderId AND s.id != :id " +
           "AND s.createdAt >= :start AND s.createdAt < :end")
    Optional<Shipping> findDuplicateForUpdate(
        @Param("orderId") UUID orderId,
        @Param("id") UUID id,
        @Param("start") Instant start,
        @Param("end") Instant end
    );
}
