package com.fashion.notification.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fashion.notification.common.enums.EnumError;
import com.fashion.notification.common.util.SplitCamelCase;
import com.fashion.notification.dto.response.kafka.PermissionRegisteredEvent;
import com.fashion.notification.exception.ServiceException;
import com.fashion.notification.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionServiceImpl implements PermissionService{
    RequestMappingHandlerMapping handlerMapping;

    @Override
    public List<PermissionRegisteredEvent> listEndPoints() {
        try {
            Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
            List<PermissionRegisteredEvent> permissions = new ArrayList<>();

            
            map.forEach((info, handlerMethod) -> {

                String path = info.getPathPatternsCondition()
                    .getPatternValues()
                    .stream()
                    .findFirst()
                    .orElse("");

                if (path == null || path.contains("error") || path.contains("errors"))
                    return;

                String[] arrPath = path.split("/");
                String module = arrPath[1].toUpperCase();

                String method = info.getMethodsCondition().getMethods()
                    .stream()
                    .findFirst()
                    .map(Enum::name)
                    .orElse("GET");

                String name = SplitCamelCase.convertCamelCase(
                        handlerMethod.getMethod().getName());

                permissions.add(
                    PermissionRegisteredEvent.builder()
                        .apiPath(path)
                        .method(method)
                        .module(module)
                        .service("NOTIFICATION-SERVICE")
                        .name(name)
                        .build());
            });

            return permissions;
        } catch (Exception e) {
            log.error("NOTIFICATION-SERVICE: [listEndPoints]: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.NOTIFICATION_INTERNAL_ERROR_CALL_API,"server.error.internal");
        }
    }
    
}
