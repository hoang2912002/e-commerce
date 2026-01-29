package com.fashion.payment.dto.request.thirdParty;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoRequest {
    String partnerCode;
    UUID requestId;
    BigDecimal amount;
    UUID orderId;
    String redirectUrl;
    String ipnUrl;
    String lang;
}
