package com.fashion.identity.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.identity.dto.response.AddressResponse;
import com.fashion.identity.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.identity.entity.Address;
@Mapper(
    componentModel = "spring"
)
public interface AddressMapper extends EntityMapper<AddressResponse, Address, InnerAddressResponse> {
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    @Named("toDto")
    AddressResponse toDto(Address entity);
    List<AddressResponse> toDto(List<Address> entity);

    @Named("toEntity")
    Address toEntity(AddressResponse dto);
    List<Address> toEntity(List<AddressResponse> dto);

    @Named("toInnerEntity")
    InnerAddressResponse toInnerEntity(Address entity);
    List<InnerAddressResponse> toInnerEntity(List<Address> entity);
}
