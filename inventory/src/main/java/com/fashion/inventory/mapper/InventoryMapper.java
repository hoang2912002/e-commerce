package com.fashion.inventory.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.inventory.dto.request.InventoryRequest;
import com.fashion.inventory.dto.request.WareHouseRequest;
import com.fashion.inventory.dto.response.InventoryResponse;
import com.fashion.inventory.dto.response.InventoryResponse.InnerInventoryResponse;
import com.fashion.inventory.entity.Inventory;
@Mapper(
    componentModel = "spring",
    uses = {

    }
)
public interface InventoryMapper extends EntityMapper<InventoryResponse, Inventory, InnerInventoryResponse, InventoryRequest>{
    InventoryMapper INSTANCE = Mappers.getMapper(InventoryMapper.class);

    @Named("toDto")
    InventoryResponse toDto(Inventory entity);
    List<InventoryResponse> toDto(List<Inventory> entity);

    @Named("toEntity")
    Inventory toEntity(InventoryResponse dto);
    List<Inventory> toEntity(List<InventoryResponse> dto);

    @Named("toInnerEntity")
    InnerInventoryResponse toInnerEntity(Inventory entity);
    List<InnerInventoryResponse> toInnerEntity(List<Inventory> entity);

    @Named("toValidated")
    @Mapping(target = "id", ignore = true)
    Inventory toValidated(InventoryRequest dto);
}
