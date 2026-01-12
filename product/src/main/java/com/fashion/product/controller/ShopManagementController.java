package com.fashion.product.controller;

import java.util.List;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.dto.request.ShopManagementRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ShopManagementResponse;
import com.fashion.product.entity.ShopManagement;
import com.fashion.product.mapper.ShopManagementMapper;
import com.fashion.product.service.ShopManagementService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/shopManagements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopManagementController {
    ShopManagementMapper shopManagementMapper;
    ShopManagementService shopManagementService;

    @PostMapping("")
    @ApiMessageResponse("shop.management.success.create")
    public ResponseEntity<ShopManagementResponse> createShopManagement(
        @RequestBody @Validated(ShopManagementRequest.Create.class) ShopManagementRequest shopManagementRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.shopManagementService.createShopManagement(shopManagementRequest));
    }

    @PutMapping("")
    @ApiMessageResponse("shop.management.success.update")
    public ResponseEntity<ShopManagementResponse> updateShopManagement(
        @RequestBody @Valid ShopManagementRequest shopManagementRequest
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.shopManagementService.updateShopManagement(shopManagementRequest));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("shop.management.success.get.single")
    public ResponseEntity<ShopManagementResponse> getShopManagementById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(this.shopManagementService.getShopManagementById(id));
    }

    @GetMapping("")
    @ApiMessageResponse("shop.management.success.get.all")
    public ResponseEntity<PaginationResponse<List<ShopManagementResponse>>> getAllShopManagement(
        @ModelAttribute SearchRequest request
    ){
        return ResponseEntity.ok(this.shopManagementService.getAllShopManagement(request));
    }
    
    @DeleteMapping("/{id}")
    @ApiMessageResponse("shop.management.success.delete")
    public ResponseEntity<Void> deleteShopManagementById(
        @PathVariable("id") UUID id
    ){
        return ResponseEntity.ok(null);
    }
}
