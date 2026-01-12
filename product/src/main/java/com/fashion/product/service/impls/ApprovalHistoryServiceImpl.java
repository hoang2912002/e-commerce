package com.fashion.product.service.impls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.util.AsyncUtils;
import com.fashion.product.common.util.FormatTime;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.ApprovalHistoryResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.RoleResponse;
import com.fashion.product.dto.response.UserResponse;
import com.fashion.product.entity.ApprovalHistory;
import com.fashion.product.entity.ApprovalMaster;
import com.fashion.product.entity.Product;
import com.fashion.product.entity.ShopManagement;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.intergration.IdentityClient;
import com.fashion.product.mapper.ApprovalHistoryMapper;
import com.fashion.product.repository.ApprovalHistoryRepository;
import com.fashion.product.repository.ApprovalMasterRepository;
import com.fashion.product.repository.ProductRepository;
import com.fashion.product.repository.ShopManagementRepository;
import com.fashion.product.security.SecurityUtils;
import com.fashion.product.service.ApprovalHistoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovalHistoryServiceImpl implements ApprovalHistoryService{
    ApprovalHistoryRepository approvalHistoryRepository;
    ApprovalMasterRepository approvalMasterRepository;
    ApprovalHistoryMapper approvalHistoryMapper;
    IdentityClient identityClient;
    ProductRepository productRepository;
    ShopManagementRepository shopManagementRepository;

    public final static String ENTITY_TYPE_PRODUCT = "PRODUCT";
    public final static String ENTITY_TYPE_INVENTORY = "INVENTORY";
    public final static String ENTITY_TYPE_SHOP_MANAGEMENT = "SHOP_MANAGEMENT";

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public ApprovalHistoryResponse createApprovalHistory(ApprovalHistory approvalHistory, boolean skipCheckPeriodDataExist, String entityType) {
        log.info("PRODUCT-SERVICE: [createApprovalHistory] Start create approval history");
        try {
            Optional<UUID> optionalId = SecurityUtils.getCurrentUserId();
            CompletableFuture<UserResponse> userFuture = (optionalId.isPresent())
                ? AsyncUtils.fetchAsync(() -> identityClient.getUserById(optionalId.get()))
                : CompletableFuture.completedFuture(null);

            CompletableFuture.allOf(userFuture).join();
            UserResponse userRes = userFuture.join();

            ApprovalMaster approvalMaster = getApprovalMaster(approvalHistory, entityType);

            validateUserPermission(userRes, approvalMaster);
            Object validatedEntity = null;
            if (!skipCheckPeriodDataExist) {
                validatedEntity = handleApprovalBusinessRules(approvalHistory, approvalMaster, null);
            }
            LocalDateTime approvedAt = LocalDateTime.now();
            String note = !approvalHistory.getNote().isBlank() ? approvalHistory.getNote().toLowerCase() : "create existing approval request";
            approvalHistory.setNote(String.format("Create %s - %s with status %s at: %s",
                entityType.toLowerCase(),
                note,
                approvalMaster.getStatus().toString(),
                FormatTime.StringDateLocalDateTime(approvedAt)
            ));
            return null;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createApprovalHistory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public ApprovalHistoryResponse updateApprovalHistory(ApprovalHistory approvalHistory,
            boolean skipCheckPeriodDataExist, String entityType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateApprovalHistory'");
    }

    @Override
    public ApprovalHistoryResponse getApprovalHistoryById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getApprovalHistoryById'");
    }

    @Override
    public PaginationResponse<List<ApprovalHistoryResponse>> getAllApprovalHistories(SearchRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllApprovalHistories'");
    }

    @Override
    public void deleteApprovalHistory(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteApprovalHistory'");
    }

    @Override
    public void handleApprovalHistoryUpSertProduct(Product product, UUID productId, String entityType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleApprovalHistoryUpSertProduct'");
    }

    @Override
    public ApprovalHistory lockAndGetApprovalHistory(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lockAndGetApprovalHistory'");
    }

    @Override
    public boolean checkApprovalHistoryForUpShop(ShopManagement shopManagement, boolean skipCreateNextApproval) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkApprovalHistoryForUpShop'");
    }

    @Override
    public boolean checkApprovalHistoryForUpSertOrder(Product product) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkApprovalHistoryForUpSertOrder'");
    }
    

    private ApprovalMaster getApprovalMaster(ApprovalHistory approvalHistory, String entityType) {
        try {
            Map<UUID, ApprovalMaster> masterMap = this.approvalMasterRepository
                .findAllByEntityType(entityType)
                .stream()
                .collect(Collectors.toMap(
                    ApprovalMaster::getId,
                    Function.identity(),
                    (a, b) -> a
                ));
            
            ApprovalMaster master = null;
            // Nếu không có id từ quy trình phê duyệt
            if (approvalHistory.getApprovalMaster() == null ||
                approvalHistory.getApprovalMaster().getId() == null) {

                master = masterMap.values().stream()
                    .filter(a -> a.getStatus() == ApprovalMasterEnum.PENDING)
                    .findFirst()
                    .orElse(null);
            }
            else{
                master = masterMap.get(
                    approvalHistory.getApprovalMaster().getId()
                );
            }
    
            if (master == null) {
                throw new ServiceException(
                    EnumError.PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ID,
                    "approvalMaster.id.notFound"
                );
            }
    
            return master;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getApprovalMaster] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void validateUserPermission(UserResponse user, ApprovalMaster approvalMaster) {
        if (user.getRole().getId() == approvalMaster.getRoleId()) {
            throw new ServiceException(
                    EnumError.PRODUCT_PERMISSION_ACCESS_DENIED,
                    "permission.access.deny"
            );
        }
    }

    private Object handleApprovalBusinessRules(
        ApprovalHistory approvalHistory, 
        ApprovalMaster master, 
        ApprovalHistory approvalHistoryDB // approval history trong db trước khi update
    ) {
        try {
            ApprovalMasterEnum status = master.getStatus();
            UUID requestId = approvalHistory.getRequestId();
    
            List<ApprovalHistory> historyList = this.approvalHistoryRepository.findAllByRequestId(requestId);
            ApprovalHistory last = historyList.isEmpty() ? null : historyList.getLast();
            // Update ko callback
            if(approvalHistoryDB != null){
                if(last == null || last.getId() == approvalHistoryDB.getId()){
                    throw new ServiceException(
                        EnumError.PRODUCT_APPROVAL_HISTORY_CURRENT_ERR_MATCHING,
                        "approval.history.last.current.not.matching"
                    );
                }
                return null;
            }
            return switch (master.getEntityType()) {
                case ENTITY_TYPE_PRODUCT -> handleProductApproval(status, last, requestId);
                case ENTITY_TYPE_INVENTORY -> handleInventoryApproval(requestId);
                case ENTITY_TYPE_SHOP_MANAGEMENT -> handleShopManagementApproval(status, last, requestId);
                default -> throw new ServiceException(
                        EnumError.PRODUCT_INTERNAL_ERROR_CALL_API,
                        "entityType.not.supported"
                );
            };
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [handleApprovalBusinessRules] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private Product handleProductApproval(
        ApprovalMasterEnum status, 
        ApprovalHistory approvalHistory, 
        UUID requestId
    ){
        try {
            Product product = productRepository.findById(requestId).orElseGet(null);
    
            if (product == null) {
                throw new ServiceException(
                        EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,
                        "product.not.found.id",
                        Map.of("id", requestId)
                );
            }
    
            if (approvalHistory == null) {
                if (!status.equals(ApprovalMasterEnum.PENDING)) {
                    throw new ServiceException(
                        EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                        "approval.history.current.not.pending",
                        Map.of("name", product.getName())
                    );
                }
                return product; // ok
            }
    
            ApprovalMasterEnum lastStatus = approvalHistory.getApprovalMaster().getStatus();            
            switch (lastStatus) {
                case PENDING -> {
                    if (status != ApprovalMasterEnum.APPROVED) {
                        throw new ServiceException(
                            EnumError.PRODUCT_PRODUCT_DATA_EXISTED_APPROVAL_PENDING,
                            "product.data.existed.approval.pending",
                            Map.of("name", product.getName())
                        );
                    }
                }
                case APPROVED -> {
                    if (status != ApprovalMasterEnum.NEEDS_ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_PRODUCT_DATA_EXISTED_NAME,
                            "approval.history.last.approved.current.not.needsAdjustment",
                            Map.of("name", product.getName())
                        );
                    }
                }
                case REJECTED -> {
                    if (status != ApprovalMasterEnum.NEEDS_ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                            "approval.history.last.rejected.current.not.needsAdjustment",
                            Map.of("name", product.getName())
                        );
                    }
                }
                case NEEDS_ADJUSTMENT -> {
                    if (status != ApprovalMasterEnum.ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                            "approval.history.last.needsAdjustment.current.not.adjustment",
                            Map.of("name", product.getName())
                        );
                    }
                }
                case ADJUSTMENT -> {
                    if (status != ApprovalMasterEnum.FINISHED_ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                            "approval.history.last.adjustment.current.not.finishedAdjustment",
                            Map.of("name", product.getName())
                        );
                    }
                }
                case FINISHED_ADJUSTMENT -> {
                    if (status != ApprovalMasterEnum.APPROVED) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                            "approval.history.last.finishedAdjustment.current.not.approved",
                            Map.of("name", product.getName())
                        );
                    }
                }
                default -> throw new ServiceException(
                    EnumError.PRODUCT_INTERNAL_ERROR_CALL_API,
                    "approval.history.status.invalid.flow"
                );
            }
            return product;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [handleProductApproval] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private Object handleInventoryApproval(UUID requestId){
        return null;
    }

    private ShopManagement handleShopManagementApproval(
        ApprovalMasterEnum status,
        ApprovalHistory lastHistory,
        UUID shopManagementId
    ){
        try {
            ShopManagement shopManagement = this.shopManagementRepository.findById(shopManagementId).orElseGet(null);
        
            if (shopManagement == null) {
                throw new ServiceException(
                        EnumError.PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID,
                        "shop.management.not.found.id",
                        Map.of("id", shopManagementId)
                );
            }
        
            if (lastHistory == null) {
                if (!status.equals(ApprovalMasterEnum.PENDING)) {
                    throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                            "approval.history.current.not.pending",
                            Map.of("name", shopManagement.getName())
                    );
                }
                return shopManagement; // ok
            }
            
            ApprovalMasterEnum lastStatus = lastHistory.getApprovalMaster().getStatus();            
            switch (lastStatus) {
                case PENDING -> {
                    if (status != ApprovalMasterEnum.APPROVED) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_PENDING_CANNOT_ADD_HISTORY,
                            "product.data.existed.approval.pending",
                            Map.of("name", shopManagement.getName())
                        );
                    }
                }
                case APPROVED -> {
                    if (status != ApprovalMasterEnum.NEEDS_ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_APPROVED_CANNOT_ADD_HISTORY,
                            "approval.history.last.approved.current.not.needsAdjustment",
                            Map.of("name", shopManagement.getName())
                        );
                    }
                }
                case REJECTED -> {
                    if (status != ApprovalMasterEnum.NEEDS_ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                            "approval.history.last.rejected.current.not.needsAdjustment",
                            Map.of("name", shopManagement.getName())
                        );
                    }
                }
                case NEEDS_ADJUSTMENT -> {
                    if (status != ApprovalMasterEnum.ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_NEED_ADJUSTMENT_CANNOT_ADD_HISTORY,
                            "approval.history.last.needsAdjustment.current.not.adjustment",
                            Map.of("name", shopManagement.getName())
                        );
                    }
                }
                case ADJUSTMENT -> {
                    if (status != ApprovalMasterEnum.FINISHED_ADJUSTMENT) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_ADJUSTMENT_CANNOT_ADD_HISTORY,
                            "approval.history.last.adjustment.current.not.finishedAdjustment",
                            Map.of("name", shopManagement.getName())
                        );
                    }
                }
                case FINISHED_ADJUSTMENT -> {
                    if (status != ApprovalMasterEnum.APPROVED) {
                        throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_FINISHED_ADJUSTMENT_CANNOT_ADD_HISTORY,
                            "approval.history.last.finishedAdjustment.current.not.approved",
                            Map.of("name", shopManagement.getName())
                        );
                    }
                }
                default -> throw new ServiceException(
                    EnumError.PRODUCT_INTERNAL_ERROR_CALL_API,
                    "approval.history.status.invalid.flow"
                );
            }
            return shopManagement;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [handleProductApproval] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }


}
