package com.fashion.identity.service;

import java.util.List;

import com.fashion.identity.dto.request.search.role.RoleSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.RoleResponse;
import com.fashion.identity.entity.Role;

public interface RoleService {
    RoleResponse createRole(Role role);

    RoleResponse updateRole(Role role);

    RoleResponse getRoleById(Long id);

    PaginationResponse getAllRole(RoleSearchRequest request);

    void deleteRoleById(Long id);

    boolean existsBySlug(String slug, Long id);

    Role findRawRoleById(Long id);

    Role lockRoleById(Long id);
}
