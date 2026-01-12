package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.ApprovalMasterRequest;
import com.fashion.product.dto.response.ApprovalMasterResponse;
import com.fashion.product.dto.response.ApprovalMasterResponse.InnerApprovalMasterResponse;
import com.fashion.product.entity.ApprovalMaster;
@Mapper(
    componentModel = "spring"
)
public interface ApprovalMasterMapper extends EntityMapper<ApprovalMasterResponse, ApprovalMaster,InnerApprovalMasterResponse, ApprovalMasterRequest> {
    ApprovalMasterMapper INSTANCE = Mappers.getMapper(ApprovalMasterMapper.class);

    @Named("toDto")
    ApprovalMasterResponse toDto(ApprovalMaster entity);
    List<ApprovalMasterResponse> toDto(List<ApprovalMaster> entity);

    @Named("toDtoNotRelationship")
    ApprovalMasterResponse toDtoNotRelationship(ApprovalMaster entity);

    @Named("toEntity")
    ApprovalMaster toEntity(ApprovalMasterResponse dto);
    List<ApprovalMaster> toEntity(List<ApprovalMasterResponse> dto);

    @Named("toInnerEntity")
    InnerApprovalMasterResponse toInnerEntity(ApprovalMaster entity);
    List<InnerApprovalMasterResponse> toInnerEntity(List<ApprovalMaster> entity);

    @Named("toValidated")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "role.id", target = "roleId")
    ApprovalMaster toValidated(ApprovalMasterRequest dto);
}
