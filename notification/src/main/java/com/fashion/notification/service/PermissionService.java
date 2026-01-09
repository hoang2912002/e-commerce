package com.fashion.notification.service;

import java.util.List;

import com.fashion.notification.dto.response.kafka.PermissionRegisteredEvent;

public interface PermissionService {
    List<PermissionRegisteredEvent> listEndPoints();
}
