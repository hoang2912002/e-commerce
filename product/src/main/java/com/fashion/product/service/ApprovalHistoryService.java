package com.fashion.product.service;

import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.ApprovalHistoryResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.ApprovalHistory;
import com.fashion.product.entity.Product;
import com.fashion.product.entity.ShopManagement;

public interface ApprovalHistoryService {
    ApprovalHistoryResponse createApprovalHistory(ApprovalHistory approvalHistory, boolean skipCheckPeriodDataExist, String entityType);
    ApprovalHistoryResponse updateApprovalHistory(ApprovalHistory approvalHistory, boolean skipCheckPeriodDataExist, String entityType);
    ApprovalHistoryResponse getApprovalHistoryById(Long id);
    PaginationResponse<List<ApprovalHistoryResponse>> getAllApprovalHistories(SearchRequest request);
    void deleteApprovalHistory(Long id);
    void handleApprovalHistoryUpSertProduct(
        Product product, UUID productId, // Đây là id để kiểm tra tạo mới hay cập nhật
        String entityType
    );
    boolean checkApprovalHistoryForUpShop(ShopManagement shopManagement, boolean skipCreateNextApproval);
    boolean checkApprovalHistoryForUpSertOrder(Product product);
}
