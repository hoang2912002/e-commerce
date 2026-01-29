package com.fashion.payment.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fashion.payment.common.enums.EnumError;
import com.fashion.payment.exception.ServiceException;
import com.fashion.payment.service.strategy.PaymentStrategy;


@Component
public class PaymentProcessorFactory {
    private final Map<String, PaymentStrategy> processors;

    public PaymentProcessorFactory(List<PaymentStrategy> processorList) {
        this.processors = processorList.stream()
            .collect(Collectors.toMap(PaymentStrategy::getPaymentMethod, Function.identity()));
    }

    public PaymentStrategy getProcessor(String paymentMethod) {
        PaymentStrategy processor = processors.get(paymentMethod.toLowerCase());
        
        if (processor == null) {
            throw new ServiceException(EnumError.PAYMENT_PAYMENT_NOT_FOUND_PAYMENT_METHOD,"payment.not.found.paymentMethod");
        }
        return processor;
    }
}
