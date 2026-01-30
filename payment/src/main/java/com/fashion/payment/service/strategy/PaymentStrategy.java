package com.fashion.payment.service.strategy;

import java.util.Map;
import java.util.UUID;

import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.entity.Payment;

public interface PaymentStrategy {
    PaymentResponse process(Payment payment, UUID eventId);
    String getPaymentMethod();
}
