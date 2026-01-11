package com.fashion.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fashion.identity.entity.Address;
import com.fashion.identity.entity.User;

public interface AddressRepository extends JpaRepository<Address, UUID>, JpaSpecificationExecutor<Address>{
    @Query("SELECT a FROM Address a WHERE " +
       "(:userId IS NULL OR a.user.id = :userId) AND " +
       "(:smId IS NULL OR a.shopManagementId = :smId) AND " +
       "a.address = :address AND " +
       "a.district = :district AND " +
       "a.province = :province AND " +
       "a.ward = :ward")
    Optional<Address> findDuplicateForCreate(
        @Param("userId") UUID userId, 
        @Param("smId") UUID smId, 
        @Param("address") String address,
        @Param("district") String district,
        @Param("province") String province,
        @Param("ward") String ward
    );

    @Query("SELECT a FROM Address a WHERE " +
       "(:userId IS NULL OR a.user.id = :userId) AND " +
       "(:smId IS NULL OR a.shopManagementId = :smId) AND " +
       "a.address = :address AND a.district = :district AND " +
       "a.province = :province AND a.ward = :ward AND " +
       "a.id <> :excludeId")
    Optional<Address> findDuplicateForUpdate(
        @Param("userId") UUID userId, 
        @Param("smId") UUID smId, 
        @Param("address") String address,
        @Param("district") String district,
        @Param("province") String province,
        @Param("ward") String ward,
        @Param("excludeId") UUID excludeId
    );
}
