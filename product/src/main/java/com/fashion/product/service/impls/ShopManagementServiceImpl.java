package com.fashion.product.service.impls;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.response.ApiResponse;
import com.fashion.product.common.util.ConvertUuidUtil;
import com.fashion.product.common.util.SlugUtil;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.AddressResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ShopManagementResponse;
import com.fashion.product.dto.response.UserResponse;
import com.fashion.product.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.product.dto.response.UserResponse.InnerUserResponse;
import com.fashion.product.entity.Promotion;
import com.fashion.product.entity.ShopManagement;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.intergration.IdentityClient;
import com.fashion.product.mapper.ShopManagementMapper;
import com.fashion.product.repository.ShopManagementRepository;
import com.fashion.product.service.ShopManagementService;

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
    @Transactional(rollbackFor = ServiceException.class)
    public ShopManagementResponse createShopManagement(ShopManagement shopManagement) {
        log.info("PRODUCT-SERVICE: [createShopManagement] Start create shop management");
        try {
            UserResponse userResponse = this.getUserResponse(shopManagement.getUserId());
            String slug = SlugUtil.toSlug(shopManagement.getName());
            this.checkPromotionExistByCode(shopManagement.getName(), null);
            shopManagement.setSlug(slug);
            shopManagement.setUserId(userResponse.getId());
            ShopManagement smCreate = this.shopManagementRepository.saveAndFlush(shopManagement);

            // Create approval history

            return this.shopManagementMapper.toDto(smCreate);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createShopManagement] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public ShopManagementResponse updateShopManagement(ShopManagement shopManagement) {
        log.info("PRODUCT-SERVICE: [updateShopManagement] Start create shop management");
        try {
            ShopManagement upSM = this.shopManagementRepository.lockShopManagementById(shopManagement.getId());
            if(upSM == null){
                throw new ServiceException(
                EnumError.PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID,
                "shop.management.not.found.id");
            }
            UserResponse userResponse = this.getUserResponse(shopManagement.getUserId());
            String slug = SlugUtil.toSlug(shopManagement.getName());
            this.checkPromotionExistByCode(shopManagement.getName(), shopManagement.getId());

            // Check important field change ?
            Map<String, Object[]> changedFields = this.detectChangedFields(upSM, shopManagement);

            boolean isImportantChange = changedFields.keySet().stream().anyMatch(IMPORTANT_FIELDS::contains);
            upSM.setSlug(slug);
            upSM.setUserId(userResponse.getId());
            if(isImportantChange){
                // Check approval history
                throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
            }
            else{
                upSM.setDescription(shopManagement.getDescription());
            }
            return this.shopManagementMapper.toDto(this.shopManagementRepository.save(upSM));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateShopManagement] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, readOnly = true)
    public ShopManagementResponse getShopManagementById(UUID id) {
        try {
            ShopManagement shopManagement = this.shopManagementRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID,
                    "shop.management.not.found.id")
            );

            RequestAttributes context = RequestContextHolder.getRequestAttributes();
            // Scatter-Gather Pattern (Mô hình Phân tán - Thu thập)
            // 1. Xử lý User Future: Kiểm tra userId trước khi gọi
            CompletableFuture<UserResponse> userFuture;
            if (shopManagement.getUserId() != null) {
                userFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        RequestContextHolder.setRequestAttributes(context);
                        return identityClient.getUserById(shopManagement.getUserId()).getData();
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                });
            } else {
                userFuture = CompletableFuture.completedFuture(null);
            }

            // 2. Xử lý Address Future: Kiểm tra addressId trước khi gọi
            CompletableFuture<AddressResponse> addressFuture;
            if (shopManagement.getAddressId() != null) {
                addressFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        RequestContextHolder.setRequestAttributes(context);
                        return identityClient.getAddressById(shopManagement.getAddressId()).getData();
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                });
            } else {
                addressFuture = CompletableFuture.completedFuture(null);
            }

            // Đợi cả 2 (nếu có)
            CompletableFuture.allOf(userFuture, addressFuture).join();
            
            UserResponse user = userFuture.join();
            AddressResponse address = addressFuture.join();

            ShopManagementResponse response = shopManagementMapper.toDto(shopManagement);

            // Map User nếu kết quả trả về không null
            if (user != null) {
                response.setUser(InnerUserResponse.builder()
                    .id(user.getId()).fullName(user.getFullName())
                    .email(user.getEmail()).phoneNumber(user.getPhoneNumber())
                    .avatar(user.getAvatar()).build());
            }

            // Map Address nếu kết quả trả về không null
            if (address != null) {
                response.setAddress(InnerAddressResponse.builder()
                    .id(address.getId()).address(address.getAddress())
                    .district(address.getDistrict()).province(address.getProvince())
                    .ward(address.getWard()).build());
            }

            return response;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getShopManagementById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    @Override
    public PaginationResponse<List<ShopManagementResponse>> getAllShopManagement(SearchRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllShopManagement'");
    }

    @Override
    public void deleteShopManagementById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShopManagementById'");
    }

    @Override
    public ShopManagement findRawShopManagementBySlug(String slug, UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findRawShopManagementBySlug'");
    }

    @Override
    public ShopManagement findRawShopManagementById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findRawShopManagementById'");
    }

    @Override
    public ShopManagement findRawShopManagementByIdForUpdate(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findRawShopManagementByIdForUpdate'");
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

    private UserResponse getUserResponse(UUID id){
        try {
            ApiResponse<UserResponse> apiResponse = this.identityClient.getUserById(id);
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
    private void checkPromotionExistByCode(String name, UUID excludeId){
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
}
