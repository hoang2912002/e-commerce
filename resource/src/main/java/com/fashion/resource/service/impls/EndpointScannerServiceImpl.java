package com.fashion.resource.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fashion.resource.common.util.SplitCamelCase;
import com.fashion.resource.dto.response.kafka.KafkaPermissionRegisterResponse;
import com.fashion.resource.dto.response.system.PermissionResponse.InnerPermissionResponse;
import com.fashion.resource.service.EndpointScannerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EndpointScannerServiceImpl implements EndpointScannerService {
    RequestMappingHandlerMapping handlerMapping;

    @Override
    public List<KafkaPermissionRegisterResponse> listPermission() {
        Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();

        List<KafkaPermissionRegisterResponse> permissions = new ArrayList<>();

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
                    KafkaPermissionRegisterResponse.builder()
                            .apiPath(path)
                            .method(method)
                            .module(module)
                            .service("RESOURCE-SERVICE")
                            .name(name)
                            .build());
        });

        return permissions;
    }

}
