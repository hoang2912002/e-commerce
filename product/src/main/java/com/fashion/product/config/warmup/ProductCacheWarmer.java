package com.fashion.product.config.warmup;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fashion.product.dto.request.ProductRequest.InnerInternalProductRequest;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.entity.Product;
import com.fashion.product.entity.ProductSku;
import com.fashion.product.mapper.ProductMapper;
import com.fashion.product.repository.ProductRepository;
import com.fashion.product.service.ProductService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCacheWarmer {
    ProductRepository productRepository;
    ProductService productService;
    ProductMapper productMapper;
    
    /**
     * Refresh cache periodically (every 10 minutes)
     */
    @Scheduled(fixedDelay = 600000)
    public void refreshCache() {
        this.productService.warmProduct();
    }
}
