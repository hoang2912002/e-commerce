package com.fashion.payment.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Component
@ConfigurationProperties(prefix = "momo")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentMomoProperties {
    String partnerCode;
    String endPoint;
}
