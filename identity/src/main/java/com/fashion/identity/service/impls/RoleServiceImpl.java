package com.fashion.identity.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.common.util.ConvertUuidUtil;
import com.fashion.identity.common.util.PageableUtils;
import com.fashion.identity.common.util.SlugUtil;
import com.fashion.identity.common.util.SpecificationUtils;
import com.fashion.identity.dto.request.search.role.RoleSearchModel;
import com.fashion.identity.dto.request.search.role.RoleSearchOption;
import com.fashion.identity.dto.request.search.role.RoleSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.RoleResponse;
import com.fashion.identity.entity.Permission;
import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.mapper.RoleMapper;
import com.fashion.identity.repository.RoleRepository;
import com.fashion.identity.service.PermissionService;
import com.fashion.identity.service.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleServiceImpl implements RoleService {
    final RoleRepository roleRepository;
    final RoleMapper roleMapper;
    final PermissionService permissionService;

    @Value("${role.slug.admin}")
    public static String roleAdmin;
    
    @Value("${role.slug.user}")
    public static String roleUser;
    
    @Value("${role.slug.seller}")
    public static String roleSeller;

    @Override
    public RoleResponse createRole(Role role){
        log.info("IDENTITY-SERVICE: [createRole] start create role ....");
        try {
            final String slug = SlugUtil.toSlug(role.getName());
            if(existsBySlug(slug, null)){
                throw new ServiceException(EnumError.IDENTITY_ROLE_DATA_EXISTED_NAME, "role.exist.name",Map.of("name", role.getName()));
            }
            List<Permission> permissions = new ArrayList<>();
            if(role.getPermissions() != null && !role.getPermissions().isEmpty()){
                List<Long> pIdList = role.getPermissions().stream().map(
                    p -> p.getId()
                ).toList();
                permissions = this.permissionService.getPermissionByListId(pIdList);
            }
            final Role createRole = Role.builder()
            .name(role.getName())
            .slug(slug)
            .activated(true)
            .permissions(permissions)
            .build();
            return roleMapper.toDto(this.roleRepository.save(createRole));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [createRole] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly=true)
    public boolean existsBySlug(String slug, Long checkId){
        try {
            return Objects.nonNull(checkId) ? 
                this.roleRepository.existsBySlugAndIdNot(slug, checkId) : 
                this.roleRepository.existsBySlug(slug);
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [existsBySlug] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public RoleResponse updateRole(Role role) {
        log.info("IDENTITY-SERVICE: [updateRole] update create role ....");
        try {
            final String slug = SlugUtil.toSlug(role.getName());
            final Role updateRole = this.lockRoleById(role.getId());
            
            if(existsBySlug(slug, role.getId())){
                throw new ServiceException(EnumError.IDENTITY_ROLE_DATA_EXISTED_NAME, "role.exist.name",Map.of("name", role.getName()));
            }
            List<Permission> permissions = new ArrayList<>();
            if(role.getPermissions() != null && !role.getPermissions().isEmpty()){
                List<Long> pIdList = role.getPermissions().stream().map(
                    p -> p.getId()
                ).toList();
                permissions = this.permissionService.getPermissionByListId(pIdList);
            }
            updateRole.setName(role.getName());
            updateRole.setSlug(slug);
            updateRole.setActivated(true);
            updateRole.setPermissions(permissions);
            return roleMapper.toDto(this.roleRepository.save(updateRole));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [updateRole] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly=true)
    public RoleResponse getRoleById(Long id) {
        try {
            return roleMapper.toDto(this.findRawRoleById(id));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [getRoleById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly=true)
    public PaginationResponse<List<RoleResponse>> getAllRole(RoleSearchRequest request) {
        try {
            // handle get all
            RoleSearchOption option = request.getSearchOption();
            RoleSearchModel model = request.getSearchModel();

            List<String> allowedField = List.of("createdAt");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                option.getPage(), 
                option.getSize(), 
                option.getSort(), 
                allowedField, 
                "createdAt", 
                Sort.Direction.DESC
            );
            List<String> fields = SpecificationUtils.getFieldsSearch(Role.class);
            Specification<Role> spec = new SpecificationUtils<Role>()
                    .equal("activated", model.getActivated())
                    .likeAnyFieldIgnoreCase(model.getQ(), fields)
                    .build();
            Page<Role> roles = this.roleRepository.findAll(spec, pageRequest);
            List<RoleResponse> roleResponses = roles.getContent().stream().map(roleMapper::toDto).toList();


            return PageableUtils.buildPaginationResponse(pageRequest, roles, roleResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [getAllRole] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteRoleById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteRoleById'");
    }

    @Override
    @Transactional(readOnly = true)
    public Role findRawRoleById(Long id) {
        return this.roleRepository.findById(id)
                .orElseThrow(() -> new ServiceException(EnumError.IDENTITY_ROLE_ERR_NOT_FOUND_ID, "role.not.found.id",Map.of("id", id)));
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Role lockRoleById(Long id){
        try {
            return this.roleRepository.lockRoleById(id);
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: [lockRoleById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
