package com.fashion.api_gateway.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ExceptionConfig {
    @Bean
    public WebProperties.Resources webPropertiesResources() {
        return new WebProperties.Resources();
    }
}
