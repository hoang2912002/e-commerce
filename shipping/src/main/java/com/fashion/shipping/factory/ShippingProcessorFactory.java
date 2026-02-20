package com.fashion.shipping.factory;

import java.security.Provider.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fashion.shipping.common.enums.EnumError;
import com.fashion.shipping.exception.ServiceException;
import com.fashion.shipping.service.strategy.ShippingStrategy;


@Component
public class ShippingProcessorFactory {
    private final Map<String, ShippingStrategy> processors;

    public ShippingProcessorFactory(List<ShippingStrategy> processorList) {
        this.processors = processorList.stream()
            .collect(Collectors.toMap(ShippingStrategy::getProviderName, Function.identity()));
    }

    public ShippingStrategy getProcessor(String providerName) {
        ShippingStrategy processor = processors.get(providerName.toUpperCase());
        
        if (processor == null) {
            throw new ServiceException(EnumError.SHIPPING_SHIPPING_PROVIDER_NOT_SUPPORTED,"shipping.provider.not.support");
        }
        return processor;
    }
}
