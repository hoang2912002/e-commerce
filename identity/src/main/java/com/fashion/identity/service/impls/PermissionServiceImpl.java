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
import com.fashion.identity.repository.PermissionRepository;
import com.fashion.identity.service.PermissionService;

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

    @Override
    @Transactional(rollbackFor = ServiceException.class)
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
            return this.permissionMapper.toDto(this.permissionRepository.saveAndFlush(createPermission)); 

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [createPermission] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PermissionResponse updatePermission(Permission permission) {
        log.info("IDENTITY-SERVICE: [updatePermission] start update permission ....");
        try {
            final Permission updatePermission = this.lockPermissionById(permission.getId());
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

            return this.permissionMapper.toDto(this.permissionRepository.saveAndFlush(updatePermission)); 
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [updatePermission] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        try {
            return this.permissionMapper.toDto(this.findRawPermissionById(id));
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
    
    @Override
    @Transactional(readOnly = true)
    public Permission lockPermissionById(Long id){
        try {
            return this.permissionRepository.lockPermissionById(id);
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [lockPermissionById] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Permission findRawPermissionById(Long id) {
        return this.permissionRepository.findById(id)
                .orElseThrow(() -> new ServiceException(EnumError.IDENTITY_PERMISSION_ERR_NOT_FOUND_ID, "permission.not.found.id",Map.of("id", id)));
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
}
