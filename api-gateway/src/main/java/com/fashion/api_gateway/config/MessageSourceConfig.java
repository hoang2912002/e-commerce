package com.fashion.api_gateway.config;

import java.util.Locale;

import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;

@Configuration
public class MessageSourceConfig {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:language/message");
        // Nhiều file vd: "classpath:language/messages_account", "classpath:language/messages_user"
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    @Bean
    public AcceptHeaderLocaleContextResolver localeResolver() {
        AcceptHeaderLocaleContextResolver resolver = new AcceptHeaderLocaleContextResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
}
