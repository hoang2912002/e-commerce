package com.fashion.identity.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.identity.common.util.ConvertUuidUtil;
import com.fashion.identity.dto.request.AddressRequest;
import com.fashion.identity.dto.request.AddressRequest.InnerAddressRequest;
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

    // @Named("toValidated")
    // @Mapping(target = "id", source = "id", qualifiedByName = "addressToUuid")
    // Address toValidated(AddressRequest dto);
    

    // // Format id thành type UUID
    // @Named("addressToUuid")
    // default UUID toUuid(Object id) {
    //     if (id == null) return null;
    //     return ConvertUuidUtil.toUuid(id);
    // }
}
