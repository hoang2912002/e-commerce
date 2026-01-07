package com.fashion.identity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.identity.entity.Address;

public interface AddressRepository extends JpaRepository<Address, UUID>, JpaSpecificationExecutor<Address>{
    
}
