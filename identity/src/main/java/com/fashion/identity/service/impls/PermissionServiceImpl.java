package com.fashion.identity.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.common.enums.MicroserviceTypeEnum;
import com.fashion.identity.common.util.ConvertUuidUtil;
import com.fashion.identity.common.util.PageableUtils;
import com.fashion.identity.common.util.SpecificationUtils;
import com.fashion.identity.dto.request.search.user.UserSearchModel;
import com.fashion.identity.dto.request.search.user.UserSearchOption;
import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.PermissionResponse;
import com.fashion.identity.dto.response.RoleResponse;
import com.fashion.identity.entity.Permission;
import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.mapper.PermissionMapper;
import com.fashion.identity.properties.cache.IdentityServiceCacheProperties;
import com.fashion.identity.repository.PermissionRepository;
import com.fashion.identity.service.PermissionService;
import com.fashion.identity.service.provider.CacheProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    IdentityServiceCacheProperties identityServiceCacheProperties;
    CacheProvider cacheProvider;

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public PermissionResponse createPermission(Permission permission) {
        log.info("IDENTITY-SERVICE: [createPermission] start create permission ....");
        try {
            final String method = permission.getMethod().toUpperCase();
            final String module = permission.getModule().toUpperCase();
            final String apiPath = permission.getApiPath();
            final String service = permission.getService();
            
            // Kiểm tra tên service
            MicroserviceTypeEnum.fromValue(permission.getService());

            this.checkPermissionExist(apiPath, method, service, null);
            Permission createPermission = Permission.builder()
            .name(permission.getName())
            .apiPath(apiPath)
            .method(method)
            .module(module)
            .service(service)
            .activated(true)
            .build();

            PermissionResponse permissionResponse = this.permissionMapper.toDto(this.permissionRepository.saveAndFlush(createPermission));
            this.updatePermissionCache(permissionResponse);
            return permissionResponse; 

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [createPermission] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public PermissionResponse updatePermission(Permission permission) {
        log.info("IDENTITY-SERVICE: [updatePermission] start update permission ....");
        try {
            final Permission updatePermission = this.permissionRepository.lockPermissionById(permission.getId());
            final String method = permission.getMethod().toUpperCase();
            final String module = permission.getModule().toUpperCase();
            final String apiPath = permission.getApiPath();
            final String service = permission.getService();

            // Kiểm tra tên service
            MicroserviceTypeEnum.fromValue(permission.getService());
            this.checkPermissionExist(apiPath, method, service, permission.getId());
            updatePermission.setActivated(true);
            updatePermission.setMethod(method);
            updatePermission.setModule(module);
            updatePermission.setApiPath(apiPath);
            updatePermission.setService(service);
            updatePermission.setName(permission.getName());

            PermissionResponse permissionResponse = this.permissionMapper.toDto(this.permissionRepository.saveAndFlush(updatePermission));
            this.updatePermissionCache(permissionResponse);
            return permissionResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [updatePermission] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PermissionResponse getPermissionById(Long id, Long version) {
        try {
            String cacheKey = this.getCacheKey(id);
            String lockKey = this.getLockKey(id);
            return cacheProvider.getDataResponse(cacheKey, lockKey, version, PermissionResponse.class, () -> 
                this.permissionRepository.findById(id)
                    .map((u) -> {
                        PermissionResponse res = this.permissionMapper.toDto(u);
                        res.setVersion(System.currentTimeMillis());
                        return res;
                    })
                    .orElseThrow(
                        () -> new ServiceException(EnumError.IDENTITY_PERMISSION_ERR_NOT_FOUND_ID, "permission.not.found.id",Map.of("id", id))
                    )
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [getPermissionById] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getPermissionByListId(List<Long> listId) {
        try {
            List<Permission> lPermissions = this.permissionRepository.findAllByIdIn(listId);
            return lPermissions.size() > 0 ? lPermissions : null;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: getPermissionByListId: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PaginationResponse<List<PermissionResponse>> getAllPermissions(UserSearchRequest request) {
        try {
            UserSearchModel searchModel = request.getSearchModel();
            UserSearchOption searchOption = request.getSearchOption();

            List<String> allowedField = List.of("createdAt");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                searchOption.getPage(), 
                searchOption.getSize(), 
                searchOption.getSort(), 
                allowedField, 
                "createdAt", 
                Sort.Direction.DESC
            );
            List<String> fields = SpecificationUtils.getFieldsSearch(Permission.class);
            Specification<Permission> spec = new SpecificationUtils<Permission>()
                    .equal("activated", searchModel.getActivated())
                    .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                    .build();

            Page<Permission> permissions = this.permissionRepository.findAll(spec, pageRequest);
            List<PermissionResponse> permissionResponses = permissions.getContent().stream().map(permissionMapper::toDto).toList();

            return PageableUtils.buildPaginationResponse(pageRequest, permissions, permissionResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [getAllPermissions] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deletePermissionById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deletePermissionById'");
    }
    
    private void checkPermissionExist(String apiPath, String method, String service,Long excludeId){
        try {
            Optional<Permission> duplicate;
    
            if (excludeId == null) {
                duplicate = this.permissionRepository.findDuplicateForCreate(apiPath, method, service);
            } else {
                duplicate = this.permissionRepository.findDuplicateForUpdate(apiPath, method, service, excludeId);
            }
            duplicate.ifPresent(p -> {
                throw new ServiceException(
                    EnumError.IDENTITY_PERMISSION_DATA_EXISTED_APIPATH_METHOD_SERVICE,
                    "permission.exist.apiPath.method.service", 
                    Map.of("apiPath", apiPath, "method", method, "service", service));
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e){
            throw e;
        }
    }

    private String getCacheKey(Long id){
        return this.identityServiceCacheProperties.createCacheKey(
            this.identityServiceCacheProperties.getKeys().getPermissionInfo(),
            id
        );
    }

    private String getLockKey(Long id){
        return this.identityServiceCacheProperties.createLockKey(
            this.identityServiceCacheProperties.getKeys().getPermissionInfo(),
            id
        );
    }

    private void updatePermissionCache(PermissionResponse permissionResponse) {
        try {
            permissionResponse.setVersion(System.currentTimeMillis());
            
            String cacheKey = this.getCacheKey(permissionResponse.getId());
            cacheProvider.put(cacheKey, permissionResponse);
            
            log.info("IDENTITY-SERVICE: Updated cache for permission ID: {}", permissionResponse.getId());
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [updatePermissionCache] Error updating cache: {}", e.getMessage(), e);
        }
    } 
}
