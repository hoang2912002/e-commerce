package com.fashion.identity.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.identity.common.annotation.ApiMessageResponse;
import com.fashion.identity.dto.request.RoleRequest;
import com.fashion.identity.dto.request.search.role.RoleSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.RoleResponse;
import com.fashion.identity.mapper.RoleMapper;
import com.fashion.identity.service.RoleService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;
    RoleMapper roleMapper;

    @PostMapping("")
    @ApiMessageResponse("role.success.create")
    public ResponseEntity<RoleResponse> createRole(
        @RequestBody @Valid @Validated(RoleRequest.Create.class) RoleRequest role
    ) {        
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.createRole(roleMapper.toValidated(role)));
    }
    
    @PutMapping("")
    @ApiMessageResponse("role.success.update")
    public ResponseEntity<RoleResponse> updateRole(
        @RequestBody @Valid @Validated(RoleRequest.Update.class) RoleRequest role
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roleService.updateRole(roleMapper.toValidated(role)));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("role.success.get.single")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roleService.getRoleById(id));
    }

    @GetMapping("")
    @ApiMessageResponse("role.success.get.all")
    public ResponseEntity<PaginationResponse> getAllRole(
        @ModelAttribute RoleSearchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roleService.getAllRole(request));
    }
    
    @DeleteMapping("/{id}")
    @ApiMessageResponse("role.success.delete")
    public void deleteRoleById(
        @PathVariable("id") Long id
    ){
        this.roleService.deleteRoleById(id);
    }
}
