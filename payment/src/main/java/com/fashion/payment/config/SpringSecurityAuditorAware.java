package com.fashion.payment.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.fashion.payment.security.SecurityUtils;


@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class SpringSecurityAuditorAware {
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            return Optional.of(SecurityUtils.getCurrentUserLogin().isPresent() ? SecurityUtils.getCurrentUserLogin().get() : "");
            // return Optional.of("system");
        };
    }
}
