package com.fashion.inventory.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.inventory.common.enums.EnumError;
import com.fashion.inventory.common.enums.WareHouseStatusEnum;
import com.fashion.inventory.common.response.ApiResponse;
import com.fashion.inventory.common.util.AsyncUtils;
import com.fashion.inventory.common.util.ConvertUuidUtil;
import com.fashion.inventory.common.util.PageableUtils;
import com.fashion.inventory.common.util.SpecificationUtils;
import com.fashion.inventory.dto.request.WareHouseRequest;
import com.fashion.inventory.dto.request.search.SearchModel;
import com.fashion.inventory.dto.request.search.SearchOption;
import com.fashion.inventory.dto.request.search.SearchRequest;
import com.fashion.inventory.dto.response.PaginationResponse;
import com.fashion.inventory.dto.response.WareHouseResponse;
import com.fashion.inventory.dto.response.internal.UserResponse;
import com.fashion.inventory.dto.response.internal.UserResponse.UserInsideToken;
import com.fashion.inventory.entity.WareHouse;
import com.fashion.inventory.exception.ServiceException;
import com.fashion.inventory.intergration.IdentityClient;
import com.fashion.inventory.mapper.WareHouseMapper;
import com.fashion.inventory.properties.cache.InventoryServiceCacheProperties;
import com.fashion.inventory.repository.WareHouseRepository;
import com.fashion.inventory.security.SecurityUtils;
import com.fashion.inventory.service.WareHouseService;
import com.fashion.inventory.service.provider.CacheProvider;
import com.fashion.inventory.service.provider.WareHouseUpSertErrorProvider;
import com.fashion.inventory.service.provider.WareHouseUpdateStatusErrorProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import feign.FeignException.FeignClientException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WareHouseServiceImpl implements WareHouseService {
    final WareHouseMapper wareHouseMapper;
    final WareHouseRepository wareHouseRepository;
    final WareHouseUpSertErrorProvider wareHouseUpSertErrorProvider;
    final WareHouseUpdateStatusErrorProvider wareHouseUpdateStatusErrorProvider;
    final IdentityClient identityClient;
    final CacheProvider cacheProvider;
    final InventoryServiceCacheProperties inventoryServiceCacheProperties;
    final Executor virtualExecutor;
    @Value("${role.admin}")
    String roleAdmin;
    
    @Value("${role.seller}")
    String roleSeller;
    
    @Override
    // @Transactional(readOnly = true)
    public PaginationResponse<List<WareHouseResponse>> getAllWareHouses(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(WareHouse.class);

            Specification<WareHouse> spec = new SpecificationUtils<WareHouse>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<WareHouse> products = this.wareHouseRepository.findAll(spec, pageRequest);
            List<WareHouseResponse> productResponses = this.wareHouseMapper.toDto(products.getContent());
            return PageableUtils.<WareHouse, WareHouseResponse>buildPaginationResponse(pageRequest, products, productResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [getAllWareHouses] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public WareHouseResponse createWareHouse(WareHouseRequest request) {
        try {
            log.info("INVENTORY-SERVICE: [createWareHouse] Start create ware house");
            WareHouseResponse wareHouseResponse = this.upSertWareHouse(request, true);
            this.updateWareHouseCache(wareHouseResponse);
            return wareHouseResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [createWareHouse] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public WareHouseResponse updateWareHouse(WareHouseRequest request) {
        try {
            log.info("INVENTORY-SERVICE: [updateWareHouse] Start update ware house");
            WareHouseResponse wareHouseResponse = this.upSertWareHouse(request, false);
            this.updateWareHouseCache(wareHouseResponse);
            return wareHouseResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [updateWareHouse] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    // @Transactional(readOnly = true)
    public WareHouseResponse getWareHouseById(UUID id, Long version) {
        try {
            String cacheKey = this.getCacheKey(id);
            String lockKey = this.getLockKey(id);
            return cacheProvider.getDataResponse(cacheKey, lockKey, version, WareHouseResponse.class, () ->
                this.wareHouseRepository.findById(id)
                    .map((w) -> {
                        WareHouseResponse res = this.wareHouseMapper.toDto(w);
                        res.setVersion(System.currentTimeMillis());
                        return res;
                    })
                    .orElseThrow(() -> new ServiceException(EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID,"ware.house.not.found.id", Map.of("id", id)))
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [getWareHouseById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteWareHouseById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteWareHouseById'");
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public WareHouseResponse updateWareHouseStatus(WareHouseRequest request) {
        try {
            log.info("INVENTORY-SERVICE: [updateWareHouseStatus] Start update ware house status");
            UserInsideToken currentUser = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_USER_INVALID_ACCESS_TOKEN, "auth.accessToken.invalid")
            );
            ApiResponse<Void> userResponse = this.identityClient.validateInternalUserById(currentUser.getId(), true, request.getVersion());
            WareHouse wareHouse = this.wareHouseRepository.lockWareHouseById(request.getId()).orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID,"ware.house.not.found.id", Map.of("id", request.getId()))
            );
            wareHouse.getStatus().validateUpdateStatusAbility(request.getStatus(), wareHouseUpdateStatusErrorProvider, Map.of("status", wareHouse.getStatus()));
            wareHouse.setStatus(request.getStatus());
            return this.wareHouseMapper.toDto(this.wareHouseRepository.save(wareHouse));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [updateWareHouseStatus] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Cacheable(value = "warehouse", key = "#wareHouseId", unless = "#result == null")
    public WareHouse fetchWareHouse(UUID wareHouseId){
        return this.wareHouseRepository.findById(wareHouseId)
            .orElseThrow(() -> new ServiceException(
                EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID,
                "ware.house.not.found.id", 
                Map.of("id", wareHouseId)
            )
        );
    }
    
    private WareHouseResponse upSertWareHouse(WareHouseRequest request, boolean isCreate) {
        try {
            UserInsideToken currentUser = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ServiceException(
                    EnumError.INVENTORY_USER_INVALID_ACCESS_TOKEN, 
                    "auth.accessToken.invalid"
                ));

            final String normalizedCode = request.getCode().trim().toUpperCase();
            final String normalizedName = request.getName().trim();

            CompletableFuture<Void> userValidationFuture = AsyncUtils.fetchVoidWThread(
                () -> identityClient.validateInternalUserById(
                    currentUser.getId(), 
                    true, 
                    request.getVersion()
                ), 
                virtualExecutor
            );

            CompletableFuture<WareHouse> wareHouseFuture;
            CompletableFuture<Void> duplicateCheckFuture;

            if (isCreate) {
                wareHouseFuture = CompletableFuture.completedFuture(
                    WareHouse.builder()
                        .code(normalizedCode)
                        .name(normalizedName)
                        .location(request.getLocation())
                        .status(WareHouseStatusEnum.PENDING)
                        .activated(true)
                        .build()
                );

                duplicateCheckFuture = AsyncUtils.fetchVoidWThread(
                    () -> checkExistedWareHouse(normalizedCode, normalizedName, null),
                    virtualExecutor
                );
            } else {
                // UPDATE: Fetch existing warehouse
                wareHouseFuture = AsyncUtils.fetchAsyncWThread(
                    () -> wareHouseRepository.lockWareHouseById(request.getId())
                        .orElseThrow(() -> new ServiceException(
                            EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID,
                            "ware.house.not.found.id", 
                            Map.of("id", request.getId())
                        )),
                    virtualExecutor
                );

                // Duplicate check will happen after we have the existing warehouse
                duplicateCheckFuture = CompletableFuture.completedFuture(null);
            }

            try {
                CompletableFuture.allOf(userValidationFuture, wareHouseFuture, duplicateCheckFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }

            WareHouse wareHouse = wareHouseFuture.join();

            if (!isCreate) {
                wareHouse.getStatus().validateUpSertAbility(
                    wareHouseUpSertErrorProvider, 
                    Map.of("code", wareHouse.getCode(), "name", wareHouse.getName())
                );

                checkExistedWareHouse(normalizedCode, normalizedName, wareHouse.getId());
            }

            wareHouse.setCode(normalizedCode);
            wareHouse.setName(normalizedName);
            wareHouse.setLocation(request.getLocation());
            wareHouse.setActivated(true);

            WareHouse savedWareHouse = wareHouseRepository.save(wareHouse);
            return wareHouseMapper.toDto(savedWareHouse);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [upSertWareHouseOptimized] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
    private void checkExistedWareHouse(String code, String name, UUID excludedId){
        try {
            Optional<WareHouse> optional;
            if(excludedId == null){
                optional = this.wareHouseRepository.findDuplicateForCreate(code, name);
            } else {
                optional = this.wareHouseRepository.findDuplicateForUpdate(code, name, excludedId);
            }
            optional.ifPresent(wareHouse -> {
                if(wareHouse.getCode().equals(code)){
                    throw new ServiceException(
                        EnumError.INVENTORY_WARE_HOUSE_DATA_EXISTED_CODE, 
                        "ware.house.exist.code",
                        Map.of("code", code
                    ));
                }
                else{
                    throw new ServiceException(
                        EnumError.INVENTORY_WARE_HOUSE_DATA_EXISTED_NAME, 
                        "ware.house.exist.name",
                        Map.of("name", name
                    ));
                }
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [checkExistedWareHouse] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private String getCacheKey(UUID id){
        return this.inventoryServiceCacheProperties.createCacheKey(
            this.inventoryServiceCacheProperties.getKeys().getWareHouseInfo(),
            id
        );
    }

    private String getLockKey(UUID id){
        return this.inventoryServiceCacheProperties.createLockKey(
            this.inventoryServiceCacheProperties.getKeys().getWareHouseInfo(),
            id
        );
    }

    private void updateWareHouseCache(WareHouseResponse wareHouseResponse) {
        try {
            wareHouseResponse.setVersion(System.currentTimeMillis());
            
            String cacheKey = this.getCacheKey(wareHouseResponse.getId());
            cacheProvider.put(cacheKey, wareHouseResponse);
            
            log.info("INVENTORY-SERVICE: Updated cache for ware house ID: {}", wareHouseResponse.getId());
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [updateWareHouseCache] Error updating cache: {}", e.getMessage(), e);
        }
    } 
}
