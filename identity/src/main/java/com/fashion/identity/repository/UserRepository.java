package com.fashion.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fashion.identity.entity.User;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface UserRepository extends JpaRepository<User, UUID>,JpaSpecificationExecutor<User>{
    Optional<User> findByUserName(String userName);
    
    @Query("SELECT u FROM User u WHERE u.userName = :un OR u.email = :em OR u.phoneNumber = :ph")
    Optional<User> findDuplicateForCreate(
        @Param("un") String userName, 
        @Param("em") String email, 
        @Param("ph") String phoneNumber
    );
    // Optional<User> findByUserNameOrEmailOrPhoneNumberAndIdNot(String userName, String email, String phoneNumber, UUID id);


    @Query("SELECT u FROM User u WHERE (u.userName = :un OR u.email = :em OR u.phoneNumber = :ph) AND u.id <> :id")
    Optional<User> findDuplicateForUpdate(
        @Param("un") String userName, 
        @Param("em") String email, 
        @Param("ph") String phoneNumber, 
        @Param("id") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "0")
    })
    User lockUserById(@Param("id") UUID id);
}
