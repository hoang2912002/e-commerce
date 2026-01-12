package com.fashion.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.product.dto.request.ApprovalHistoryRequest;
import com.fashion.product.dto.response.ApprovalHistoryResponse;
import com.fashion.product.dto.response.ApprovalHistoryResponse.InnerApprovalHistoryResponse;
import com.fashion.product.entity.ApprovalHistory;

@Mapper(
    componentModel = "spring",
    uses = {
        ApprovalMasterMapper.class,
    }
)
public interface ApprovalHistoryMapper extends EntityMapper<ApprovalHistoryResponse, ApprovalHistory, InnerApprovalHistoryResponse, ApprovalHistoryRequest>{
    ApprovalHistoryMapper INSTANCE = Mappers.getMapper(ApprovalHistoryMapper.class);

    @Named("toDto")
    ApprovalHistoryResponse toDto(ApprovalHistory entity);
    List<ApprovalHistoryResponse> toDto(List<ApprovalHistory> entity);

    @Named("toDtoNotRelationship")
    ApprovalHistoryResponse toDtoNotRelationship(ApprovalHistory entity);

    @Named("toEntity")
    ApprovalHistory toEntity(ApprovalHistoryResponse dto);
    List<ApprovalHistory> toEntity(List<ApprovalHistoryResponse> dto);

    @Named("toInnerEntity")
    InnerApprovalHistoryResponse toInnerEntity(ApprovalHistory entity);
    List<InnerApprovalHistoryResponse> toInnerEntity(List<ApprovalHistory> entity);

    @Named("toValidated")
    ApprovalHistory toValidated(ApprovalHistoryRequest dto);
}
