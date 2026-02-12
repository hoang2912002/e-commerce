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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.dto.request.ApprovalMasterRequest;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.ApprovalMasterResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.service.ApprovalMasterService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/approvalMasters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApprovalMasterController {
    ApprovalMasterService approvalMasterService;

    @PostMapping("")
    @ApiMessageResponse("approval.master.success.create")
    public ResponseEntity<ApprovalMasterResponse> createApprovalMaster(
        @RequestBody @Validated(ApprovalMasterRequest.Create.class) ApprovalMasterRequest approvalMasterRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.approvalMasterService.createApprovalMaster(approvalMasterRequest));
    }
    
    @PutMapping("")
    @ApiMessageResponse("approval.master.success.update")
    public ResponseEntity<ApprovalMasterResponse> updateApprovalMaster(
        @RequestBody @Validated(ApprovalMasterRequest.Update.class) ApprovalMasterRequest approvalMasterRequest
    ) {
        return ResponseEntity.ok().body(this.approvalMasterService.updateApprovalMaster(approvalMasterRequest));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("approval.master.success.get.single")
    public ResponseEntity<ApprovalMasterResponse> getApprovalMasterById(
        @PathVariable("id") UUID id,
        @RequestParam("version") Long version
    ) {
        return ResponseEntity.ok().body(this.approvalMasterService.getApprovalMasterById(id,version));
    }
    
    @GetMapping("")
    @ApiMessageResponse("approval.master.success.get.all")
    public ResponseEntity<PaginationResponse<List<ApprovalMasterResponse>>> getAllApprovalMaster(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok().body(this.approvalMasterService.getAllApprovalMaster(request));
    }
    
    @DeleteMapping("/{id}")
    @ApiMessageResponse("approval.master.success.delete")
    public ResponseEntity<Void> deleteApprovalMasterById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.noContent().build();
    }
}
