package com.fashion.shipping.common.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageUtil {
    MessageSource messageSource;

    public String getMessage(String key) {
        return messageSource.getMessage(
            key,
            null,
            LocaleContextHolder.getLocale()
        );
    }

    public String getMessage(String key, LocaleContext localeContext) {
        return messageSource.getMessage(
            key,
            null,
            localeContext.getLocale()
        );
    }

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(
            key,
            args,
            LocaleContextHolder.getLocale()
        );
    }

    public String getMessage(String key, LocaleContext localeContext, Object... args) {
        return messageSource.getMessage(
            key,
            args,
            localeContext.getLocale()
        );
    }
}
