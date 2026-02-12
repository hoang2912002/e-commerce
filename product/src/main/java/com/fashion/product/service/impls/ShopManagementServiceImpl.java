package com.fashion.product.service.impls;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.response.ApiResponse;
import com.fashion.product.common.util.AsyncUtils;
import com.fashion.product.common.util.ConvertUuidUtil;
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SlugUtil;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.ShopManagementRequest;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.AddressResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.dto.response.ShopManagementResponse;
import com.fashion.product.dto.response.UserResponse;
import com.fashion.product.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.product.dto.response.ApprovalMasterResponse;
import com.fashion.product.dto.response.CategoryResponse;
import com.fashion.product.dto.response.UserResponse.InnerUserResponse;
import com.fashion.product.dto.response.kafka.ProductApprovedEvent.InternalProductApprovedEvent;
import com.fashion.product.dto.response.kafka.ShopManagementAddressEvent;
import com.fashion.product.dto.response.kafka.ShopManagementAddressEvent.InternalShopManagementAddressEvent;
import com.fashion.product.entity.ApprovalHistory;
import com.fashion.product.entity.ApprovalMaster;
import com.fashion.product.entity.Promotion;
import com.fashion.product.entity.ShopManagement;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.intergration.IdentityClient;
import com.fashion.product.mapper.ShopManagementMapper;
import com.fashion.product.messaging.provider.ProductServiceProvider;
import com.fashion.product.properties.cache.ProductServiceCacheProperties;
import com.fashion.product.repository.ShopManagementRepository;
import com.fashion.product.service.ApprovalHistoryService;
import com.fashion.product.service.KafkaService;
import com.fashion.product.service.ShopManagementService;
import com.fashion.product.service.provider.CacheProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopManagementServiceImpl implements ShopManagementService{
    final IdentityClient identityClient;
    final ShopManagementRepository shopManagementRepository;
    final ShopManagementMapper shopManagementMapper;
    final ProductServiceProvider productServiceProvider;
    final ApprovalHistoryService approvalHistoryService;
    final ProductServiceCacheProperties productServiceCacheProperties;
    final CacheProvider cacheProvider;
    final ApplicationEventPublisher applicationEventPublisher;
    @Value("${role.admin}")
    String roleAmin;
    
    @Value("${role.seller}")
    String roleSeller;

    public static final Set<String> IMPORTANT_FIELDS = Set.of(
        "accountName",
        "accountNumber",
        "bankBranch",
        "bankName",
        "businessDateIssue",
        "businessName",
        "businessNo",
        "businessType",
        "businessPlace",
        "taxCode",
        "name"
    );

    public static final Set<String> NORMAL_FIELDS = Set.of(
        "description"
    );

    private static final Set<String> IGNORE_FIELDS = Set.of(
        "products",
        "userId",
        "addressId",
        "slug"
    );
    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public ShopManagementResponse createShopManagement(ShopManagementRequest request) {
        log.info("PRODUCT-SERVICE: [createShopManagement] Start create shop management");
        try {
            ShopManagement shopManagement = this.shopManagementMapper.toValidated(request);

            UserResponse userResponse = this.getUserResponse(shopManagement.getUserId(), request.getVersion());
            String slug = SlugUtil.toSlug(shopManagement.getName());
            this.checkShopManagementExistByName(shopManagement.getName(), null);
            shopManagement.setSlug(slug);
            shopManagement.setActivated(true);
            shopManagement.setUserId(userResponse.getId());
            ShopManagement smCreate = this.shopManagementRepository.saveAndFlush(shopManagement);

            // Create approval history
            this.approvalHistoryService.createApprovalHistory(
                ApprovalHistory.builder()
                .approvedAt(LocalDateTime.now())
                .approvalMaster(new ApprovalMaster())
                .requestId(smCreate.getId())
                .note("")
                .build(), 
                true, 
                ApprovalHistoryServiceImpl.ENTITY_TYPE_SHOP_MANAGEMENT,
                request.getVersion()
            ); 
            // Send kafka 
            ShopManagementAddressEvent addressEvent = ShopManagementAddressEvent.builder()
                .id(null)
                .address(request.getAddress().getAddress())
                .district(request.getAddress().getDistrict())
                .ward(request.getAddress().getWard())
                .province(request.getAddress().getProvince())
                .shopManagementId(smCreate.getId())
                .build();

            applicationEventPublisher.publishEvent(new InternalShopManagementAddressEvent(this, addressEvent));
            // this.productServiceProvider.produceShopManagementEventSuccess(addressEvent);

            ShopManagementResponse shopManagementResponse = this.shopManagementMapper.toDto(smCreate);
            this.updateShopManagementCache(shopManagementResponse);
            return shopManagementResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createShopManagement] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public ShopManagementResponse updateShopManagement(ShopManagementRequest request) {
        log.info("PRODUCT-SERVICE: [updateShopManagement] Start create shop management");
        try {
            ShopManagement shopManagement = this.shopManagementMapper.toValidated(request);
            ShopManagement upSM = this.shopManagementRepository.lockShopManagementById(shopManagement.getId());
            if(upSM == null){
                throw new ServiceException(
                EnumError.PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID,
                "shop.management.not.found.id");
            }
            UserResponse userResponse = this.getUserResponse(shopManagement.getUserId(), request.getVersion());
            String slug = SlugUtil.toSlug(shopManagement.getName());
            this.checkShopManagementExistByName(shopManagement.getName(), shopManagement.getId());

            // Check important field change ?
            Map<String, Object[]> changedFields = this.detectChangedFields(upSM, shopManagement);

            boolean isImportantChange = changedFields.keySet().stream().anyMatch(IMPORTANT_FIELDS::contains);
            
            if(isImportantChange){
                // Check approval history
                this.approvalHistoryService.checkApprovalHistoryForUpShop(upSM, false, request.getVersion());
                this.shopManagementMapper.toUpdate(upSM, request);
            }
            else{
                upSM.setDescription(shopManagement.getDescription());
            }
            upSM.setSlug(slug);
            upSM.setActivated(true);
            upSM.setUserId(userResponse.getId());
            // Send kafka 
            ShopManagementAddressEvent addressEvent = ShopManagementAddressEvent.builder()
                .id(null)
                .address(request.getAddress().getAddress())
                .district(request.getAddress().getDistrict())
                .ward(request.getAddress().getWard())
                .province(request.getAddress().getProvince())
                .shopManagementId(upSM.getId())
                .build();
            applicationEventPublisher.publishEvent(new InternalShopManagementAddressEvent(this, addressEvent));
            ShopManagementResponse shopManagementResponse = this.shopManagementMapper.toDto(this.shopManagementRepository.save(upSM));
            this.updateShopManagementCache(shopManagementResponse);
            return shopManagementResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateShopManagement] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, readOnly = true)
    public ShopManagementResponse getShopManagementById(UUID id, Long version) {
        try {
            String cacheKey = this.getCacheKey(id);
            String lockKey = this.getLockKey(id);
            return cacheProvider.getDataResponse(cacheKey, lockKey, version, ShopManagementResponse.class, () -> 
                this.shopManagementRepository.findById(id)
                    .map((p) -> {
                        // Scatter-Gather Pattern (Mô hình Phân tán - Thu thập)
                        // 1. Xử lý User Future: Kiểm tra userId trước khi gọi
                        CompletableFuture<UserResponse> userFuture = (p.getUserId() != null)
                            ? AsyncUtils.fetchAsync(() -> identityClient.getUserById(p.getUserId(), version))
                            : CompletableFuture.completedFuture(null);
                        
                        CompletableFuture<AddressResponse> addressFuture = (p.getAddressId() != null)
                            ? AsyncUtils.fetchAsync(() -> identityClient.getAddressById(p.getAddressId()))
                            : CompletableFuture.completedFuture(null);
                        
                        try {
                            CompletableFuture.allOf(userFuture, addressFuture).join();
                        } catch (CompletionException e) {
                            if (e.getCause() instanceof ServiceException serviceException) {
                                throw serviceException;
                            }
                            throw e;
                        }
                        
                        UserResponse user = userFuture.join();
                        AddressResponse address = addressFuture.join();

                        ShopManagementResponse shopManagementResponse = shopManagementMapper.toDto(p);
                        shopManagementResponse.setVersion(System.currentTimeMillis());


                        // Map User nếu kết quả trả về không null
                        if (user != null) {
                            shopManagementResponse.setUser(InnerUserResponse.builder()
                                .id(user.getId()).fullName(user.getFullName())
                                .email(user.getEmail()).phoneNumber(user.getPhoneNumber())
                                .avatar(user.getAvatar()).build());
                        }

                        // Map Address nếu kết quả trả về không null
                        if (address != null) {
                            shopManagementResponse.setAddress(InnerAddressResponse.builder()
                                .id(address.getId()).address(address.getAddress())
                                .district(address.getDistrict()).province(address.getProvince())
                                .ward(address.getWard()).build());
                        }
                        return shopManagementResponse;
                    }).orElse(null)
                );


        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getShopManagementById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    @Override
    public PaginationResponse<List<ShopManagementResponse>> getAllShopManagement(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(ShopManagement.class);
            Specification<ShopManagement> spec = new SpecificationUtils<ShopManagement>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<ShopManagement> sPage = this.shopManagementRepository.findAll(spec, pageRequest);
            List<ShopManagementResponse> shopManagementResponses = this.shopManagementMapper.toDto(sPage.getContent());
            return PageableUtils.<ShopManagement, ShopManagementResponse>buildPaginationResponse(pageRequest, sPage, shopManagementResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllShopManagement] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteShopManagementById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShopManagementById'");
    }

    @Override
    public Map<String, Object[]> detectChangedFields(ShopManagement oldData, ShopManagement newData) {
        Map<String, Object[]> changes = new HashMap<>();
        try {
            for (Field field : ShopManagement.class.getDeclaredFields()) {
                if(IGNORE_FIELDS.contains(field.getName())){
                    continue;
                }
                field.setAccessible(true);
                Object oldValue = field.get(oldData);
                Object newValue = field.get(newData);

                if (!Objects.equals(oldValue, newValue)) {
                    changes.put(field.getName(), new Object[]{oldValue, newValue});
                }
            }
            return changes;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [detectChangedFields] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }

    }

    @Cacheable(value = "shopManagement", key = "#shopManagementId", unless = "#result == null")
    public ShopManagement fetchShopManagement(UUID shopManagementId) {
        return shopManagementRepository.findById(shopManagementId)
            .orElseThrow(() -> new ServiceException(
                EnumError.PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID,
                "shop.management.not.found.id",
                Map.of("shopManagementId", shopManagementId)
            ));
    }

    private UserResponse getUserResponse(UUID id, Long version){
        try {
            ApiResponse<UserResponse> apiResponse = this.identityClient.getUserById(id, version);
            UserResponse userResponse = apiResponse.getData();
            String userRole = userResponse.getRole().getSlug().toUpperCase();
            if(!userRole.equals(roleAmin) && !userRole.equals(roleSeller)){
                throw new ServiceException(
                EnumError.PRODUCT_PERMISSION_ACCESS_DENIED,
                "permission.access.deny");
            }
            return userResponse;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getUserResponse] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    private void checkShopManagementExistByName(String name, UUID excludeId){
        try {
            String slug = SlugUtil.toSlug(name);
            Optional<ShopManagement> promotion;
            if(Objects.isNull(excludeId)){
                promotion = this.shopManagementRepository.findDuplicateForCreate(slug);
            } else {
                promotion = this.shopManagementRepository.findDuplicateForUpdate(slug,excludeId);
            }
            promotion.ifPresent(user -> {
                throw new ServiceException(
                    EnumError.PRODUCT_SHOP_MANAGEMENT_DATA_EXISTED_NAME, // add this enum if missing
                    "shop.management.exist.name",
                    Map.of("name", name)
                );
            });
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkPromotionExistByCode] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }

    }
    private String getCacheKey(UUID id){
        return this.productServiceCacheProperties.createCacheKey(
            this.productServiceCacheProperties.getKeys().getShopManagementInfo(),
            id
        );
    }

    private String getLockKey(UUID id){
        return this.productServiceCacheProperties.createLockKey(
            this.productServiceCacheProperties.getKeys().getShopManagementInfo(),
            id
        );
    }

    private void updateShopManagementCache(ShopManagementResponse shopManagementResponse) {
        try {
            // Set current timestamp as version
            shopManagementResponse.setVersion(System.currentTimeMillis());
            
            String cacheKey = this.getCacheKey(shopManagementResponse.getId());
            cacheProvider.put(cacheKey, shopManagementResponse);
            
            log.info("PRODUCT-SERVICE: Updated cache for shop management ID: {}", shopManagementResponse.getId());
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateShopManagementCache] Error updating cache: {}", e.getMessage(), e);
        }
    } 
}
