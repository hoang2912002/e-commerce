package com.fashion.resource.service;

import java.util.List;

import com.fashion.resource.dto.response.kafka.KafkaPermissionRegisterResponse;
import com.fashion.resource.dto.response.system.PermissionResponse.InnerPermissionResponse;

public interface EndpointScannerService {
    List<KafkaPermissionRegisterResponse> listPermission();
}
