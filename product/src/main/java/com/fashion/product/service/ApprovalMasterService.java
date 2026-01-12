package com.fashion.product.service;

import java.util.List;
import java.util.UUID;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.dto.request.ApprovalMasterRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.ApprovalMasterResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.ApprovalMaster;

public interface ApprovalMasterService {
    ApprovalMasterResponse createApprovalMaster(ApprovalMasterRequest request);
    ApprovalMasterResponse updateApprovalMaster(ApprovalMasterRequest request);
    ApprovalMasterResponse getApprovalMasterById(UUID id);
    PaginationResponse<List<ApprovalMasterResponse>> getAllApprovalMaster(SearchRequest request);
    void deleteApprovalMasterById(Long id);
    List<ApprovalMaster> findRawAllApprovalMasterByEntityType(String entityType); 
}
