package com.fashion.identity.service.impls;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.dto.response.PermissionResponse;
import com.fashion.identity.entity.Permission;
import com.fashion.identity.exception.ServiceException;
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

    @Override
    public PermissionResponse createPermission(Permission permission) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPermission'");
    }

    @Override
    public PermissionResponse updatePermission(Permission permission) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePermission'");
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPermissionById'");
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
    
}
