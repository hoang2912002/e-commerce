package com.fashion.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fashion.identity.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>,JpaSpecificationExecutor<User>{
    Optional<User> findByUserName(String userName);
}
