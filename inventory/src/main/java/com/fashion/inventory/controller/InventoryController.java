package com.fashion.inventory.controller;

import java.util.Collection;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.inventory.common.annotation.ApiMessageResponse;
import com.fashion.inventory.common.annotation.InternalEndpoint;
import com.fashion.inventory.dto.request.InventoryRequest;
import com.fashion.inventory.dto.request.InventoryRequest.InnerOrderDetail_FromOrderRequest;
import com.fashion.inventory.dto.request.search.SearchRequest;
import com.fashion.inventory.dto.response.InventoryResponse;
import com.fashion.inventory.dto.response.PaginationResponse;
import com.fashion.inventory.entity.Inventory;
import com.fashion.inventory.service.InventoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {
    InventoryService inventoryService;

    @PostMapping("")
    @ApiMessageResponse("inventory.success.create")
    public ResponseEntity<InventoryResponse> createInventory(
        @RequestBody @Validated(InventoryRequest.Create.class) InventoryRequest inventory
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.inventoryService.createInventory(inventory));
    }

    @PutMapping("")
    @ApiMessageResponse("inventory.success.update")
    public ResponseEntity<InventoryResponse> updateInventory(
        @RequestBody @Validated(InventoryRequest.Update.class) InventoryRequest inventory    
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.inventoryService.updateInventory(inventory));
    }
    
    @GetMapping("/{id}")
    @ApiMessageResponse("inventory.success.get.single")
    public ResponseEntity<InventoryResponse> getInventoryById(
        @PathVariable("id") UUID id,
        @RequestParam("version") Long version
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.inventoryService.getInventoryById(id, version));
    }
    
    @GetMapping("")
    @ApiMessageResponse("inventory.success.get.all")
    public ResponseEntity<PaginationResponse<List<InventoryResponse>>> getAllInventories(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.inventoryService.getAllInventories(request));
    }

    @DeleteMapping("/{id}")
    @ApiMessageResponse("inventory.success.delete")
    public ResponseEntity<Void> deleteInventoryById(
        @PathVariable("id") Long id
    ){
        return ResponseEntity.noContent().build();
    }

    //------------------Internal endpoint-----------------------
    @InternalEndpoint
    @GetMapping("/internal/{id}")
    @ApiMessageResponse("inventory.success.internal.get.single")
    public ResponseEntity<Inventory> getInternalInventoryById(
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
    
    @InternalEndpoint
    @PostMapping("/internal/validate-internal-inventory-and-check-quantity-available")
    @ApiMessageResponse("inventory.success.internal.get.single")
    public ResponseEntity<Void> checkInternalQuantityAvailableForOrder(
        @RequestBody Collection<InnerOrderDetail_FromOrderRequest> inventory,
        @RequestParam Long version
    ) {
        this.inventoryService.checkInternalQuantityAvailableForOrder(inventory, version);
        return ResponseEntity.noContent().build();
    }
}
