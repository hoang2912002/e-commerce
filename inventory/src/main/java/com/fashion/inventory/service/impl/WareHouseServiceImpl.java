package com.fashion.inventory.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.inventory.common.enums.EnumError;
import com.fashion.inventory.common.enums.WareHouseStatusEnum;
import com.fashion.inventory.common.response.ApiResponse;
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
import com.fashion.inventory.repository.WareHouseRepository;
import com.fashion.inventory.security.SecurityUtils;
import com.fashion.inventory.service.WareHouseService;
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
    
    @Value("${role.admin}")
    String roleAdmin;
    
    @Value("${role.seller}")
    String roleSeller;
    
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(rollbackFor = ServiceException.class)
    public WareHouseResponse createWareHouse(WareHouseRequest request) {
        try {
            log.info("INVENTORY-SERVICE: [createWareHouse] Start create ware house");
            return this.upSertWareHouse(request);
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
            return this.upSertWareHouse(request);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [updateWareHouse] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WareHouseResponse getWareHouseById(UUID id) {
        try {
            return this.wareHouseMapper.toDto(this.wareHouseRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID,"ware.house.not.found.id", Map.of("id", id))
            ));
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
    @Transactional(rollbackFor = ServiceException.class)
    public WareHouseResponse updateWareHouseStatus(WareHouseRequest request) {
        try {
            log.info("INVENTORY-SERVICE: [updateWareHouseStatus] Start update ware house status");
            UserInsideToken currentUser = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_USER_INVALID_ACCESS_TOKEN, "auth.accessToken.invalid")
            );
            ApiResponse<Void> userResponse = this.identityClient.validateInternalUserById(currentUser.getId(), true);
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
    
    private WareHouseResponse upSertWareHouse(WareHouseRequest request){
        try {
            UserInsideToken currentUser = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_USER_INVALID_ACCESS_TOKEN, "auth.accessToken.invalid")
            );
            ApiResponse<Void> userResponse = this.identityClient.validateInternalUserById(currentUser.getId(), true);
            boolean isCreate = request.getId() == null;
            WareHouse wareHouse;
            if(isCreate){
                wareHouse = this.wareHouseMapper.toValidated(request);
                wareHouse.setStatus(WareHouseStatusEnum.PENDING);
            } else{
                wareHouse = this.wareHouseRepository.lockWareHouseById(request.getId()).orElseThrow(
                    () -> new ServiceException(EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID,"ware.house.not.found.id", Map.of("id", request.getId()))
                );
                wareHouse.getStatus().validateUpSertAbility(wareHouseUpSertErrorProvider, Map.of("code", wareHouse.getCode(), "name", wareHouse.getName()));
            }
            this.checkExistedWareHouse(wareHouse.getCode(),wareHouse.getName(),wareHouse.getId());
            wareHouse.setName(request.getName());
            wareHouse.setLocation(request.getLocation());
            wareHouse.setActivated(true);
            wareHouse.setCode(request.getCode().trim().toUpperCase());
            return this.wareHouseMapper.toDto(this.wareHouseRepository.save(wareHouse));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [upSertWareHouse] Error: {}", e.getMessage(), e);
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
}
