package com.fashion.identity.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.identity.common.annotation.ApiMessageResponse;
import com.fashion.identity.dto.request.PermissionRequest;
import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.dto.response.PermissionResponse;
import com.fashion.identity.mapper.PermissionMapper;
import com.fashion.identity.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequiredArgsConstructor
@RequestMapping("/permissions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;
    PermissionMapper permissionMapper;

    @PostMapping("")
    @ApiMessageResponse("permission.success.create")
    public ResponseEntity<PermissionResponse> createPermission(
        @RequestBody PermissionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.createPermission(permissionMapper.toValidated(request)));
    }

    @PutMapping("")
    @ApiMessageResponse("permission.success.update")
    public ResponseEntity<PermissionResponse> updatePermission(@RequestBody PermissionRequest request) {
        return ResponseEntity.ok(this.permissionService.updatePermission(permissionMapper.toValidated(request)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermissionById(
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(this.permissionService.getPermissionById(id));
    }
    
    @GetMapping("")
    public ResponseEntity<PaginationResponse<List<PermissionResponse>>> getAllPermission(
        @ModelAttribute UserSearchRequest request
    ) {
        return ResponseEntity.ok(this.permissionService.getAllPermissions(request));
    }
    
    @DeleteMapping("/{id}")
    public void deletePermissionById(@PathVariable("id") Long id){
        this.permissionService.deletePermissionById(id);
    }
}
