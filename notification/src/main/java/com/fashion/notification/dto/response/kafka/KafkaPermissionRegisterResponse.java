package com.fashion.notification.dto.response.kafka;

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
public class KafkaPermissionRegisterResponse {
    Long id;
    String name;
    String apiPath;
    String method;
    String module;
    String service;
}
