package com.fashion.api_gateway.config;

import java.util.List;

import javax.swing.Spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.fashion.api_gateway.intergration.IdentityClient;


@Configuration
public class WebClientConfiguration {
    @Value("${service.identity.url}")
    String identityServiceUrl;
    @Bean
    WebClient webClient(){
        return WebClient.builder()
            .baseUrl(identityServiceUrl)
            .build();
    }

    @Bean
    IdentityClient identityClient(WebClient webClient){
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
            .builderFor(
                WebClientAdapter.create(webClient)
            ).build();
        
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }

    @Bean
    CorsWebFilter corsWebFilter(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
