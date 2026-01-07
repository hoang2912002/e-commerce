package com.fashion.identity.service;

import java.util.List;

import com.fashion.identity.dto.response.PermissionResponse;
import com.fashion.identity.entity.Permission;

public interface PermissionService {
    PermissionResponse createPermission(Permission permission);
    PermissionResponse updatePermission(Permission permission);
    PermissionResponse getPermissionById(Long id);
    List<Permission> getPermissionByListId(List<Long> listId);
}
