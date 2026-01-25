package com.fashion.order.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.order.dto.request.OrderRequest;
import com.fashion.order.dto.request.OrderRequest;
import com.fashion.order.dto.response.OrderResponse;
import com.fashion.order.dto.response.OrderResponse;
import com.fashion.order.dto.response.OrderResponse.InnerOrderResponse;
import com.fashion.order.dto.response.OrderResponse.InnerOrderResponse;
import com.fashion.order.entity.Order;
import com.fashion.order.entity.Order;

@Mapper(
    componentModel = "spring"
)
public interface OrderMapper extends EntityMapper<OrderResponse,Order,InnerOrderResponse,OrderRequest>{
    CouponMapper INSTANCE = Mappers.getMapper(CouponMapper.class);

    @Named("toDto")
    OrderResponse toDto(Order entity);
    List<OrderResponse> toDto(List<Order> entity);

    @Named("toDtoNotRelationship")
    OrderResponse toDtoNotRelationship(Order entity);

    @Named("toEntity")
    Order toEntity(OrderResponse dto);
    List<Order> toEntity(List<OrderResponse> dto);

    @Named("toInnerEntity")
    InnerOrderResponse toInnerEntity(Order entity);
    List<InnerOrderResponse> toInnerEntity(List<Order> entity);

    @Named("toValidated")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "userId", source = "user.id")
    Order toValidated(OrderRequest dto);

    @Named("toUpdate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "address.id", target = "addressId")
    void toUpdate(@MappingTarget Order entity, OrderRequest dto);
}
