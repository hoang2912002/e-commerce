package com.fashion.resource.messaging.provider;

import com.fashion.resource.dto.response.kafka.PermissionRegisteredEvent;

public interface IdentityProvider {
    void permissionRegisterEventIdentity();
    
}
