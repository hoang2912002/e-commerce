package com.fashion.order.common.util;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidatorContext;

@Component
public class ValidatorFieldUtil {
    public boolean checkNotNull(Object field, String messageKey, String fieldName, ConstraintValidatorContext context) {
        if (field == null) {
            addViolation(context, messageKey, fieldName);
            return false;
        }
        return true;
    }

    public boolean checkNotBlank(String field, String messageKey, String fieldName, ConstraintValidatorContext context) {
        if (field == null || field.trim().isEmpty()) {
            addViolation(context, messageKey, fieldName);
            return false;
        }
        return true;
    }

    public static void addViolation(
        ConstraintValidatorContext context,
        String message,
        String property
    ) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(message)
            .addPropertyNode(property)
            .addConstraintViolation();
    }
}
