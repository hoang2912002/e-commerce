package com.fashion.resource.dto.response;

import java.time.Instant;

import com.fashion.resource.common.enums.FileEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResponse {
    String id;
    String name;
    String contentType;
    Long size;
    String md5Checksum;
    String path;
    String ownerId;
    FileEnum valueType;
    String acceptUrl;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InnerFileResponse {
        String id;
        String name;
        String contentType;
        Long size;
        String md5Checksum;
        String path;
        String ownerId;
        FileEnum valueType;
        String acceptUrl;
    }
}
