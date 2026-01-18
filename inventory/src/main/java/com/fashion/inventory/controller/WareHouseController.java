package com.fashion.inventory.controller;

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

import com.fashion.inventory.common.annotation.ApiMessageResponse;
import com.fashion.inventory.dto.request.WareHouseRequest;
import com.fashion.inventory.dto.request.search.SearchRequest;
import com.fashion.inventory.dto.response.AddressResponse;
import com.fashion.inventory.dto.response.PaginationResponse;
import com.fashion.inventory.dto.response.WareHouseResponse;
import com.fashion.inventory.mapper.WareHouseMapper;
import com.fashion.inventory.service.WareHouseService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/wareHouse")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WareHouseController {
    WareHouseMapper wareHouseMapper;
    WareHouseService wareHouseService;

    @GetMapping("")
    @ApiMessageResponse("ware.house.success.get.all")
    public ResponseEntity<PaginationResponse<List<WareHouseResponse>>> getAllWareHouses(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.wareHouseService.getAllWareHouses(request));
    }


    @PostMapping("")
    @ApiMessageResponse("ware.house.success.create")
    public ResponseEntity<WareHouseResponse> createAddress(
        @RequestBody @Validated(WareHouseRequest.Create.class) WareHouseRequest request
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.wareHouseService.createWareHouse(request));
    }

    @PutMapping("")
    @ApiMessageResponse("ware.house.success.update")
    public ResponseEntity<WareHouseResponse> updateAddress(
        @RequestBody @Validated(WareHouseRequest.Update.class) WareHouseRequest request
    ) {
        return ResponseEntity.ok(this.wareHouseService.updateWareHouse(request));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("ware.house.success.get.single")
    public ResponseEntity<WareHouseResponse> getAddressById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(this.wareHouseService.getWareHouseById(id));
    }

    @DeleteMapping("/{id}")
    @ApiMessageResponse("ware.house.success.delete")
    public void deleteAddressById(
        @PathVariable("id") UUID id
    ){
        this.wareHouseService.deleteWareHouseById(id);
    }
}
