package com.fashion.payment.common.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fashion.payment.common.response.ApiResponse;

public class AsyncUtils {
    public static <T> CompletableFuture<T> fetchAsync(Supplier<ApiResponse<T>> feignCall) {
        RequestAttributes context = RequestContextHolder.getRequestAttributes();
        return CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                ApiResponse<T> response = feignCall.get();
                return (response != null) ? response.getData() : null;
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });
    }
}
