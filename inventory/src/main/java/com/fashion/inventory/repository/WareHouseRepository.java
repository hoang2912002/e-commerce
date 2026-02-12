package com.fashion.inventory.repository;

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

import com.fashion.inventory.common.enums.WareHouseStatusEnum;
import com.fashion.inventory.entity.WareHouse;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, UUID>, JpaSpecificationExecutor<WareHouse>{
    @Query("SELECT w FROM WareHouse w WHERE w.code = :code OR w.name =:name")
    Optional<WareHouse> findDuplicateForCreate(
        @Param("code") String code,
        @Param("name") String name
    );
    
    @Query("SELECT w FROM WareHouse w WHERE (w.code = :code OR w.name =:name) and w.id != :id")
    Optional<WareHouse> findDuplicateForUpdate(
        @Param("code") String code,
        @Param("name") String name,
        @Param("id") UUID id
    );

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WareHouse w WHERE w.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    Optional<WareHouse> lockWareHouseById(@Param("id") UUID id);

    WareHouse findFirstByStatusOrderByCreatedAtDesc(WareHouseStatusEnum status);
}
