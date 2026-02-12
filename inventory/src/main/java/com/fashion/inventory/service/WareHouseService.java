package com.fashion.inventory.service;

import java.util.List;
import java.util.UUID;

import com.fashion.inventory.dto.request.WareHouseRequest;
import com.fashion.inventory.dto.request.search.SearchRequest;
import com.fashion.inventory.dto.response.PaginationResponse;
import com.fashion.inventory.dto.response.WareHouseResponse;
import com.fashion.inventory.entity.WareHouse;

public interface WareHouseService {
    PaginationResponse<List<WareHouseResponse>> getAllWareHouses(SearchRequest request);
    WareHouseResponse createWareHouse(WareHouseRequest request);
    WareHouseResponse updateWareHouseStatus(WareHouseRequest request);
    WareHouseResponse updateWareHouse(WareHouseRequest request);
    WareHouseResponse getWareHouseById(UUID id, Long version);
    void deleteWareHouseById(UUID id);
    WareHouse fetchWareHouse(UUID id);
}
