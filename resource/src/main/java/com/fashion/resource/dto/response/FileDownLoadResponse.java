package com.fashion.resource.dto.response;

import org.springframework.core.io.Resource;

public record FileDownLoadResponse(String contentType, Resource resource) {}
