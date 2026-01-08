package com.fashion.identity.service;

import java.util.List;

import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.PermissionResponse;
import com.fashion.identity.entity.Permission;

public interface PermissionService {
    Permission findRawPermissionById(Long id);
    PermissionResponse createPermission(Permission permission);
    PermissionResponse updatePermission(Permission permission);
    PermissionResponse getPermissionById(Long id);
    List<Permission> getPermissionByListId(List<Long> listId);
    PaginationResponse<List<PermissionResponse>> getAllPermissions(UserSearchRequest request);
    void deletePermissionById(Long id);
    Permission lockPermissionById(Long id);
}
