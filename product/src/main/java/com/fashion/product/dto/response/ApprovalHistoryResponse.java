package com.fashion.product.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.product.dto.request.ApprovalMasterRequest.InnerApprovalMasterRequest;
import com.fashion.product.dto.response.ApprovalMasterResponse.InnerApprovalMasterResponse;

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
public class ApprovalHistoryResponse extends VersionResponse{
    Long id;
    LocalDateTime approvedAt;
    UUID requestId; 
    String note;
    InnerApprovalMasterResponse approvalMaster;
    Boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerApprovalHistoryResponse {
        Long id;
    }
}
