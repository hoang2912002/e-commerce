package com.fashion.inventory.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.inventory.dto.request.WareHouseRequest;
import com.fashion.inventory.dto.response.AddressResponse;
import com.fashion.inventory.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.inventory.dto.response.WareHouseResponse;
import com.fashion.inventory.dto.response.WareHouseResponse.InnerWareHouseResponse;
import com.fashion.inventory.entity.WareHouse;
@Mapper(
    componentModel = "spring",
    uses = {

    }
)
public interface WareHouseMapper extends EntityMapper<WareHouseResponse, WareHouse, InnerWareHouseResponse, WareHouseRequest>{
    WareHouseMapper INSTANCE = Mappers.getMapper(WareHouseMapper.class);

    @Named("toDto")
    WareHouseResponse toDto(WareHouse entity);
    List<WareHouseResponse> toDto(List<WareHouse> entity);

    @Named("toEntity")
    WareHouse toEntity(WareHouseResponse dto);
    List<WareHouse> toEntity(List<WareHouseResponse> dto);

    @Named("toInnerEntity")
    InnerWareHouseResponse toInnerEntity(WareHouse entity);
    List<InnerWareHouseResponse> toInnerEntity(List<WareHouse> entity);

    @Named("toValidated")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    WareHouse toValidated(WareHouseRequest dto);
}
