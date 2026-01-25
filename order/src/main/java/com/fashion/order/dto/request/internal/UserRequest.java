package com.fashion.order.dto.request.internal;

import java.util.UUID;

import com.fashion.order.dto.request.OrderRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

public class UserRequest {
    
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerUserRequest {
        @NotNull(groups = {OrderRequest.Create.class, OrderRequest.Update.class}, message = "order.user.id.notNull")
        UUID id;
    }
}
