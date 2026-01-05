package com.fashion.resource.service;

import java.util.List;

import com.fashion.resource.dto.response.PermissionResponse.InnerPermissionResponse;
import com.fashion.resource.dto.response.kafka.KafkaPermissionRegisterResponse;

public interface EndpointScannerService {
    List<KafkaPermissionRegisterResponse> listPermission();
}
