package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.CategoryRequest;
import com.fashion.product.dto.request.OptionRequest;
import com.fashion.product.dto.response.OptionResponse;
import com.fashion.product.dto.response.OptionResponse.InnerOptionResponse;
import com.fashion.product.dto.response.OptionResponse;
import com.fashion.product.dto.response.OptionResponse.InnerOptionResponse;
import com.fashion.product.entity.Option;
import com.fashion.product.entity.Option;

@Mapper(
    componentModel = "spring",
    uses = {
        OptionValueMapper.class
    }
)
public interface OptionMapper extends EntityMapper<OptionResponse,Option, InnerOptionResponse>{
    OptionMapper INSTANCE = Mappers.getMapper(OptionMapper.class);

    @Named("toDto")
    OptionResponse toDto(Option entity);
    List<OptionResponse> toDto(List<Option> entity);

    @Named("toDtoNotRelationship")
    OptionResponse toDtoNotRelationship(Option entity);

    @Named("toEntity")
    Option toEntity(OptionResponse dto);
    List<Option> toEntity(List<OptionResponse> dto);

    @Named("toInnerEntity")
    InnerOptionResponse toInnerEntity(Option entity);
    List<InnerOptionResponse> toInnerEntity(List<Option> entity);

    @Named("toValidated")
    // @Mapping(target = "id", source = "id")
    Option toValidated(OptionRequest dto);
}
