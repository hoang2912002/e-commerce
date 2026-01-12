package com.fashion.product.dto.request;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.product.dto.request.ApprovalMasterRequest.InnerApprovalMasterRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovalHistoryRequest {
    public interface Create {};
    public interface Update {};
    Long id;
    LocalDateTime approvedAt;
    UUID requestId; 
    String note;
    InnerApprovalMasterRequest approvalMaster;
    Boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    String entityType;

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerApprovalHistoryRequest {
        Long id;
    }
}
