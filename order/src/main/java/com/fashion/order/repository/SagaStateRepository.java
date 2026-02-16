package com.fashion.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fashion.order.entity.SagaState;
import com.fashion.order.entity.SagaStateId;

@Repository
public interface SagaStateRepository extends JpaRepository<SagaState,SagaStateId>, JpaSpecificationExecutor<SagaState>{
    
}
