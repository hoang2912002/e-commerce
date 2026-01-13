package com.fashion.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.dto.request.ApprovalHistoryRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.ApprovalHistoryResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.mapper.ApprovalHistoryMapper;
import com.fashion.product.service.ApprovalHistoryService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/approvalHistories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApprovalHistoryController {
    ApprovalHistoryMapper approvalHistoryMapper;
    ApprovalHistoryService approvalHistoryService;

    @PostMapping("")
    @ApiMessageResponse("approval.history.success.create")
    public ResponseEntity<ApprovalHistoryResponse> createApprovalHistory(
        @RequestBody @Validated(ApprovalHistoryRequest.Create.class) ApprovalHistoryRequest approvalHistoryRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.approvalHistoryService.createApprovalHistory(
            approvalHistoryMapper.toValidated(approvalHistoryRequest), 
            false, 
            approvalHistoryRequest.getEntityType()
        ));
    }
    
    @PutMapping("")
    @ApiMessageResponse("approval.history.success.update")
    public ResponseEntity<ApprovalHistoryResponse> updateApprovalHistory(
        @RequestBody @Validated(ApprovalHistoryRequest.Update.class) ApprovalHistoryRequest approvalHistoryRequest
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.approvalHistoryService.updateApprovalHistory(
            approvalHistoryMapper.toValidated(approvalHistoryRequest), 
            false, 
            approvalHistoryRequest.getEntityType()
        ));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("approval.history.success.get.single")
    public ResponseEntity<ApprovalHistoryResponse> getApprovalHistoryById(
        @PathVariable("id") Long id    
    ) {
        return ResponseEntity.ok(this.approvalHistoryService.getApprovalHistoryById(id));
    }
    
    @GetMapping("")
    @ApiMessageResponse("approval.history.success.get.all")
    public ResponseEntity<PaginationResponse<List<ApprovalHistoryResponse>>> getAllApprovalHistory(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok().body(this.approvalHistoryService.getAllApprovalHistories(request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprovalHistoryById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.noContent().build();
    }
}
