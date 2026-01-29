package com.fashion.payment.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Component
@ConfigurationProperties(prefix = "cod")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCodProperties {
    String partnerCode;
}
