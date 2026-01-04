package com.fashion.resource.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fashion.resource.common.enums.FileEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "files")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class File extends AbstractAuditingEntity<String>{
    @MongoId
    String id;

    @Override
    public String getId() {
        return id;
    }

    @Field("content_type")
    String contentType;

    Long size;

    @Field("md5_checksum")
    String md5Checksum;

    String path;

    String ownerId;

    @Field("value_type")
    FileEnum valueType;
}
