package com.fashion.product.controller;

import org.springframework.http.ResponseEntity;


import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class R4jFallback {
    /**
     * ✅ Retry fallback - Just rethrow to let next fallback handle
     */
    protected ResponseEntity<Object> resilience4jRetryFallback(Exception ex) {
        log.warn("Retry exhausted: {}", ex.getMessage());
        
        // ✅ Just rethrow to let CircuitBreaker/RateLimiter handle
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new RuntimeException(ex);
    }
    
    /**
     * ✅ CircuitBreaker fallback - Throw CallNotPermittedException
     */
    protected ResponseEntity<Object> resilience4jCircuitBreakerFallback(
        CallNotPermittedException ex
    ) {
        log.error("CircuitBreaker OPEN: {}", ex.getMessage());
        throw ex;
    }
    
    /**
     * ✅ RateLimiter fallback - Throw RequestNotPermitted
     */
    protected ResponseEntity<Object> resilience4jRateLimiterFallback(
        RequestNotPermitted ex
    ) {
        log.warn("RateLimiter exceeded: {}", ex.getMessage());
        throw ex;
    }
}
