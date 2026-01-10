package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.OptionRequest;
import com.fashion.product.dto.request.OptionValueRequest;
import com.fashion.product.dto.response.OptionValueResponse;
import com.fashion.product.dto.response.OptionValueResponse.InnerOptionValueResponse;
import com.fashion.product.entity.OptionValue;

@Mapper(
    componentModel = "spring"
)
public interface OptionValueMapper extends EntityMapper<OptionValueResponse, OptionValue, InnerOptionValueResponse>{
    OptionValueMapper INSTANCE = Mappers.getMapper(OptionValueMapper.class);
    
    @Named("toDto")
    OptionValueResponse toDto(OptionValue entity);
    List<OptionValueResponse> toDto(List<OptionValue> entity);

    @Named("toDtoNotRelationship")
    OptionValueResponse toDtoNotRelationship(OptionValue entity);

    @Named("toEntity")
    OptionValue toEntity(OptionValueResponse dto);
    List<OptionValue> toEntity(List<OptionValueResponse> dto);

    @Named("toInnerEntity")
    InnerOptionValueResponse toInnerEntity(OptionValue entity);
    List<InnerOptionValueResponse> toInnerEntity(List<OptionValue> entity);

    @Named("toValidated")
    // @Mapping(target = "id", source = "id")
    OptionValue toValidated(OptionValueRequest dto);
}
