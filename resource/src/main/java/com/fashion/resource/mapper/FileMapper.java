package com.fashion.resource.mapper;

import java.security.Permission;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fashion.resource.dto.response.FileResponse;
import com.fashion.resource.dto.response.FileResponse.InnerFileResponse;
import com.fashion.resource.entity.File;

@Mapper(
    componentModel = "spring"
)
public interface FileMapper extends EntityMapper<FileResponse, File, InnerFileResponse>{
    FileMapper INSTANCE = Mappers.getMapper(FileMapper.class);

    @Named("toDto")
    FileResponse toDto(File entity);
    List<FileResponse> toDto(List<File> entity);

    @Named("toEntity")
    File toEntity(FileResponse dto);
    List<File> toEntity(List<FileResponse> dto);

    @Named("toInnerEntity")
    InnerFileResponse toInnerEntity(File entity);
    List<InnerFileResponse> toInnerEntity(List<File> entity);
}
