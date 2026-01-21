package com.fashion.product.service.impls;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.util.AsyncUtils;
import com.fashion.product.common.util.ConvertUuidUtil;
import com.fashion.product.common.util.FormatTime;
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.ApprovalHistoryResponse;
import com.fashion.product.dto.response.ApprovalMasterResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.RoleResponse;
import com.fashion.product.dto.response.UserResponse;
import com.fashion.product.dto.response.UserResponse.UserInsideToken;
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
import com.fashion.product.service.ProductSkuService;
import com.fashion.product.service.provider.ApprovalHistoryInventoryErrorProvider;
import com.fashion.product.service.provider.ApprovalHistoryProductErrorProvider;
import com.fashion.product.service.provider.ApprovalHistorySmErrorProvider;
import com.fashion.product.service.provider.ApprovalHistoryUpSertErrorProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApprovalHistoryServiceImpl implements ApprovalHistoryService{
    ApprovalHistoryRepository approvalHistoryRepository;
    ApprovalMasterRepository approvalMasterRepository;
    ApprovalHistoryMapper approvalHistoryMapper;
    IdentityClient identityClient;
    ProductRepository productRepository;
    ProductSkuService productSkuService;
    ShopManagementRepository shopManagementRepository;
    ApprovalHistoryUpSertErrorProvider historyCreateUpdateErrorProvider;
    ApprovalHistoryProductErrorProvider historyProductErrorProvider;
    ApprovalHistorySmErrorProvider historyShopManagementErrorProvider;
    ApprovalHistoryInventoryErrorProvider historyInventoryErrorProvider;

    public final static String ENTITY_TYPE_PRODUCT = "PRODUCT";
    public final static String ENTITY_TYPE_INVENTORY = "INVENTORY";
    public final static String ENTITY_TYPE_SHOP_MANAGEMENT = "SHOP_MANAGEMENT";
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public ApprovalHistoryResponse createApprovalHistory(ApprovalHistory approvalHistory, boolean skipCheckPeriodDataExist, String entityType) {
        log.info("PRODUCT-SERVICE: [createApprovalHistory] Start create approval history");
        try {
            UserInsideToken user = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new ServiceException(EnumError.PRODUCT_USER_INVALID_ACCESS_TOKEN,"auth.accessToken.invalid")
            );
            UserResponse userRes = identityClient.getUserById(ConvertUuidUtil.toUuid(user.getId())).getData();

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
            approvalHistory.setApprovedAt(approvedAt);
            approvalHistory.setApprovalMaster(approvalMaster);
            approvalHistory.setActivated(true);
            ApprovalHistory saved = approvalHistoryRepository.save(approvalHistory);

            if(validatedEntity instanceof Product product && approvalMaster.getStatus().equals(ApprovalMasterEnum.APPROVED)
            ){
                // Tạo tồn kho cho sản phẩm
                this.productSkuService.validateAndMapSkuToInventoryRequests(product);
            }
            return approvalHistoryMapper.toDto(saved);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createApprovalHistory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public ApprovalHistoryResponse updateApprovalHistory(ApprovalHistory approvalHistory, boolean skipCheckPeriodDataExist, String entityType) {
        log.info("PRODUCT-SERVICE: [updateApprovalHistory] Start update approval history");
        try {
            UserInsideToken user = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new ServiceException(EnumError.PRODUCT_USER_INVALID_ACCESS_TOKEN,"auth.accessToken.invalid")
            );
            UserResponse userRes = identityClient.getUserById(ConvertUuidUtil.toUuid(user.getId())).getData();

            ApprovalHistory uApprovalHistory = this.approvalHistoryRepository.lockApprovalHistoryById(approvalHistory.getId()).orElseThrow(
                () -> new ServiceException(
                    EnumError.PRODUCT_APPROVAL_HISTORY_ERR_NOT_FOUND_ID,
                    "approval.history.not.found.id",
                    Map.of("id", approvalHistory.getId())
                )
            );

            ApprovalMaster approvalMaster = getApprovalMaster(approvalHistory, entityType);
            this.validateUserPermission(userRes, approvalMaster);
            if (!skipCheckPeriodDataExist) {
                handleApprovalBusinessRules(approvalHistory, approvalMaster, uApprovalHistory);
            }
            LocalDateTime approvedAt = LocalDateTime.now();
            String note = !approvalHistory.getNote().isBlank() ? approvalHistory.getNote().toLowerCase() : "create existing approval request";
            approvalHistory.setNote(String.format("Update %s - %s with status %s at: %s",
                entityType.toLowerCase(),
                note,
                approvalMaster.getStatus().toString(),
                FormatTime.StringDateLocalDateTime(approvedAt)
            ));
            approvalHistory.setApprovedAt(approvedAt);
            approvalHistory.setActivated(true);
            ApprovalHistory saved = approvalHistoryRepository.save(approvalHistory);
            return approvalHistoryMapper.toDto(saved);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateApprovalHistory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalHistoryResponse getApprovalHistoryById(Long id) {
        try {
            ApprovalHistory approvalHistory = this.approvalHistoryRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.PRODUCT_APPROVAL_HISTORY_ERR_NOT_FOUND_ID,
                    "approval.history.not.found.id",
                    Map.of("id", id)
                )
            );
            return approvalHistoryMapper.toDto(approvalHistory);
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getApprovalHistoryById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PaginationResponse<List<ApprovalHistoryResponse>> getAllApprovalHistories(SearchRequest request) {
        try {
            SearchOption searchOption = request.getSearchOption();
            SearchModel searchModel = request.getSearchModel();

            List<String> allowedField = List.of("createdAt");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                searchOption.getPage(), 
                searchOption.getSize(), 
                searchOption.getSort(), 
                allowedField, 
                "createdAt", 
                Sort.Direction.DESC
            );
            List<String> fields = SpecificationUtils.getFieldsSearch(ApprovalHistory.class);
            Specification<ApprovalHistory> spec = new SpecificationUtils<ApprovalHistory>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<ApprovalHistory> approvalHistories = this.approvalHistoryRepository.findAll(spec, pageRequest);
            List<ApprovalHistoryResponse> approvalHistoryResponses = this.approvalHistoryMapper.toDto(approvalHistories.getContent());
            return PageableUtils.<ApprovalHistory, ApprovalHistoryResponse>buildPaginationResponse(pageRequest, approvalHistories, approvalHistoryResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllApprovalMaster] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteApprovalHistory(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteApprovalHistory'");
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void handleApprovalHistoryUpSertProduct(Product product, boolean isCreate, String entityType) {
        try {
            Map<ApprovalMasterEnum, ApprovalMaster> masterMap = this.getApprovalMasterMap(entityType);

            // 2. Tìm lịch sử phê duyệt cuối cùng (Dùng phương thức Repository đã tối ưu LIMIT 1)
            Optional<ApprovalHistory> lastHistoryOpt = this.approvalHistoryRepository
                    .findFirstByRequestIdAndApprovalMasterIdInOrderByApprovedAtDesc(product.getId(), 
                        masterMap.values().stream().map(ApprovalMaster::getId).toList());
            
            // 3. Xử lý logic dựa trên trạng thái (State-driven logic)
            this.processApprovalState(product, isCreate, lastHistoryOpt, masterMap, entityType);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [handleApprovalHistoryUpSertProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public boolean checkApprovalHistoryForUpShop(ShopManagement shopManagement, boolean skipCreateNextApproval) {
        try {
            Map<ApprovalMasterEnum, ApprovalMaster> masterMap = this.getApprovalMasterMap(ENTITY_TYPE_SHOP_MANAGEMENT);
            
            Optional<ApprovalHistory> lastHistoryOpt = this.approvalHistoryRepository
                .findFirstByRequestIdAndApprovalMasterIdInOrderByApprovedAtDesc(shopManagement.getId(), 
                    masterMap.values().stream().map(ApprovalMaster::getId).toList());
            
            ApprovalMaster nextMaster = !skipCreateNextApproval ? resolveNextApprovalMaster(lastHistoryOpt, masterMap) : null;
            return this.checkShopManagementUpdatable(
                lastHistoryOpt, 
                shopManagement,
                nextMaster
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkApprovalHistoryForUpShop] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public boolean checkApprovalHistoryForUpSertOrder(Product product) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkApprovalHistoryForUpSertOrder'");
    }

    @Override
    @Transactional(readOnly = true)
    public void validateInternalApprovalHistoryByRequestId(UUID requestId){
        try {
            ApprovalHistory finalApproval = this.approvalHistoryRepository.findFirstByRequestIdOrderByApprovedAtDesc(requestId).orElseThrow(
                () -> new ServiceException(EnumError.PRODUCT_APPROVAL_HISTORY_ERR_NOT_FOUND_LAST_PRODUCT,"approval.history.not.found.requestId", Map.of("productId", requestId))
            );  
            finalApproval.getApprovalMaster().getStatus().validateAbilityUpsertInventory(historyInventoryErrorProvider,Map.of("productId", requestId));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllInternalApprovalHistoryByRequestId] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
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
                    "approval.master.not.found.id"
                );
            }
    
            return master;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getApprovalMaster] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void validateUserPermission(UserResponse user, ApprovalMaster approvalMaster) {
        if (user.getRole().getId() != approvalMaster.getRoleId()) {
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

                // Kiểm tra Status bị thay đổi khi Update
                if (!approvalHistoryDB.getApprovalMaster().getStatus().equals(master.getStatus())) {
                    throw new ServiceException(
                        EnumError.PRODUCT_APPROVAL_HISTORY_STATUS_CANNOT_BE_CHANGED, // Tạo mã lỗi mới
                        "approval.history.status.readonly"
                    );
                }

                if(last == null || last.getId() != approvalHistoryDB.getId()){
                    throw new ServiceException(
                        EnumError.PRODUCT_APPROVAL_HISTORY_CURRENT_NOT_LATEST,
                        "approval.history.not.latest",
                        Map.of("id", requestId)
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
            Product product = productRepository.findById(requestId).orElseThrow(
                () -> new ServiceException(
                        EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,
                        "product.not.found.id",
                        Map.of("id", requestId)
                )
            );
    
            if (product == null) {
                throw new ServiceException(
                        EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,
                        "product.not.found.id",
                        Map.of("id", requestId)
                );
            }
            ApprovalMasterEnum currentStatus = (approvalHistory == null) ? ApprovalMasterEnum.PENDING : approvalHistory.getApprovalMaster().getStatus();
            Map<String, Object> errorParams = Map.of("name", product.getName());
            if (approvalHistory == null) {
                if (!status.equals(ApprovalMasterEnum.PENDING)) {
                    throw new ServiceException(
                        EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                        "approval.history.current.not.pending",
                        Map.of("name", product.getName())
                    );
                }
            } else {
                currentStatus.validateTransition(status, historyCreateUpdateErrorProvider, errorParams);

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
        ApprovalHistory approvalHistory,
        UUID shopManagementId
    ){
        try {
            ShopManagement shopManagement = this.shopManagementRepository.findById(shopManagementId).orElseThrow(
                () -> new ServiceException(
                        EnumError.PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID,
                        "shop.management.not.found.id",
                        Map.of("id", shopManagementId)
                )
            );
            ApprovalMasterEnum currentStatus = (approvalHistory == null) ? ApprovalMasterEnum.PENDING : approvalHistory.getApprovalMaster().getStatus();
            Map<String, Object> errorParams = Map.of("name", shopManagement.getName());
            if (approvalHistory == null) {
                if (!status.equals(ApprovalMasterEnum.PENDING)) {
                    throw new ServiceException(
                            EnumError.PRODUCT_APPROVAL_MASTER_DATA_STATUS_REJECTED_CANNOT_ADD_HISTORY,
                            "approval.history.current.not.pending",
                            Map.of("name", shopManagement.getName())
                    );
                }
            } else {
                currentStatus.validateTransition(status, historyCreateUpdateErrorProvider, errorParams);

            }
            return shopManagement;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [handleProductApproval] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private Map<ApprovalMasterEnum, ApprovalMaster> getApprovalMasterMap(String entityType) {
        List<ApprovalMaster> masters = approvalMasterRepository.findAllByEntityType(entityType.toUpperCase());
        if (masters.isEmpty()) {
            throw new ServiceException(EnumError.PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ENTITY, 
                "approval.master.not.found.entityType", Map.of("entityType", entityType));
        }
        return masters.stream().collect(Collectors.toMap(ApprovalMaster::getStatus, Function.identity()));
    }

    private void processApprovalState(Product product, boolean isCreate, 
        Optional<ApprovalHistory> lastHistoryOpt, 
        Map<ApprovalMasterEnum, ApprovalMaster> masterMap,
        String entityType
    ) {
        try {
            Map<String, Object> errorParams = Map.of("name", product.getName());
            ApprovalMaster pendingMaster = masterMap.get(ApprovalMasterEnum.PENDING);

            // Kiểm tra Master Data cơ bản
            if (pendingMaster == null) {
                throw new ServiceException(EnumError.PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ENTITY_TYPE_STATUS, 
                    "approval.master.not.found.pending", Map.of("entityType", entityType));
            }

            // TRƯỜNG HỢP 1: TẠO MỚI (productId == null)
            if (isCreate) {
                lastHistoryOpt.ifPresent(h -> h.getApprovalMaster().getStatus()
                    .validateCreateAbility(historyProductErrorProvider, errorParams));

                this.createApprovalHistory(
                    buildHistory(null, pendingMaster, product.getId(), "Create new approval request"), 
                    true, entityType);
                return; 
            }

            // TRƯỜNG HỢP 2: CẬP NHẬT (productId != null)
            lastHistoryOpt.ifPresentOrElse(
                lastHistory -> {
                    // Đã có history -> Thực hiện Update
                    ApprovalMasterEnum currentStatus = lastHistory.getApprovalMaster().getStatus();
                    currentStatus.validateUpdateAbility(product.getId());

                    ApprovalMaster nextMaster = (currentStatus == ApprovalMasterEnum.PENDING) 
                        ? lastHistory.getApprovalMaster() 
                        : masterMap.get(ApprovalMasterEnum.FINISHED_ADJUSTMENT);

                    this.updateApprovalHistory(
                        buildHistory(lastHistory.getId(), nextMaster, product.getId(), "Update existing approval request"), 
                        true, entityType);
                },
                () -> {
                    this.createApprovalHistory(
                        buildHistory(null, pendingMaster, product.getId(), "Re-create missing approval request"), 
                        true, entityType);
                }
            );   
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [processApprovalState] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private ApprovalHistory buildHistory(Long id, ApprovalMaster master, UUID requestId, String note) {
        return ApprovalHistory.builder()
            .id(id)
            .approvalMaster(master)
            .requestId(requestId)
            .approvedAt(LocalDateTime.now())
            .note(note)
            .build();
    }

    private ApprovalMaster resolveNextApprovalMaster(Optional<ApprovalHistory> lastHistoryOpt, Map<ApprovalMasterEnum, ApprovalMaster> masterMap) {
        return lastHistoryOpt.map(lastHistory -> {
            ApprovalMasterEnum lastStatus = lastHistory.getApprovalMaster().getStatus();

            return switch (lastStatus) {
                case PENDING -> lastHistory.getApprovalMaster();
                case ADJUSTMENT -> masterMap.get(ApprovalMasterEnum.FINISHED_ADJUSTMENT);
                default -> null;
            };
        }).orElseGet(() -> masterMap.get(ApprovalMasterEnum.PENDING));
    }

    private boolean checkShopManagementUpdatable(Optional<ApprovalHistory> lastHistoryOpt, ShopManagement shop, ApprovalMaster nextMaster) {
        try {
            Map<String, Object> errorParams = Map.of("name", shop.getName());
            return lastHistoryOpt.map(lastHistory -> {
                ApprovalMasterEnum lastStatus = lastHistory.getApprovalMaster().getStatus();
                lastStatus.validateCrUpAbility(nextMaster.getStatus(),historyShopManagementErrorProvider,errorParams);

                if(lastStatus == ApprovalMasterEnum.ADJUSTMENT && 
                    nextMaster.getStatus().equals(ApprovalMasterEnum.FINISHED_ADJUSTMENT)
                ){
                    this.createApprovalHistory(
                        this.buildHistory(null,nextMaster,shop.getId(),"Re-create missing approval request"),
                        true,
                        ENTITY_TYPE_SHOP_MANAGEMENT
                    );
                }
                return true;
            }).orElseGet(() -> {
                if(nextMaster != null){
                    ApprovalHistoryResponse create = this.createApprovalHistory(
                        this.buildHistory(null,nextMaster,shop.getId(),"Re-create missing approval request"),
                        true,
                        ENTITY_TYPE_SHOP_MANAGEMENT
                    );
                    return create != null;
                }
                throw new ServiceException(
                    EnumError.PRODUCT_APPROVAL_HISTORY_ERR_NOT_FOUND_LAST_SHOP_MANAGEMENT,
                    "approval.history.not.found.status.shop.management",
                    errorParams
                );
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkShopManagementUpdatable] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

}
