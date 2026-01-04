package com.fashion.resource.messaging.provider;

import com.fashion.resource.dto.response.kafka.KafkaPermissionRegisterResponse;

public interface IdentityProvider {
    void permissionRegisterEventIdentity();
    
}
