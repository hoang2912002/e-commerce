package com.fashion.product.dto.request;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fashion.product.dto.request.ApprovalMasterRequest.InnerApprovalMasterRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "approval.history.id.notNull", groups = {Update.class})
    Long id;
    LocalDateTime approvedAt;

    @NotNull(message = "approval.history.requestId.notNull", groups = {Create.class, Update.class})
    UUID requestId; 
    String note;

    @Valid
    InnerApprovalMasterRequest approvalMaster;
    Boolean activated;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    @NotNull(message = "approval.master.entityType.notNull", groups = {Create.class, Update.class})
    String entityType;

    @NotNull(message = "server.version.not.be.null", groups = {Create.class, Update.class})
    Long version;

    @Builder
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerApprovalHistoryRequest {
        Long id;
    }
}
