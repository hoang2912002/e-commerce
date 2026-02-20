package com.fashion.shipping.common.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fashion.shipping.common.response.ApiResponse;

public class AsyncUtils {
    /**
     * @param CompletableFuture
     * @param supplyAsync => have output, dont have input
     * @param runAsync => dont have input output
     * @return
     */
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

    public static <T> CompletableFuture<T> fetchAsyncWThread(Supplier<T> localCall, Executor executor){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return CompletableFuture.supplyAsync(() -> {
            try {
                SecurityContextHolder.setContext(securityContext);
                RequestContextHolder.setRequestAttributes(requestAttributes);
                return localCall.get();
            } finally {
                SecurityContextHolder.clearContext();
                RequestContextHolder.resetRequestAttributes();
            }
        }, executor);
    }

    public static CompletableFuture<Void> fetchVoidWThread(Runnable task, Executor executor) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return CompletableFuture.runAsync(() -> {
            try {
                SecurityContextHolder.setContext(securityContext);
                RequestContextHolder.setRequestAttributes(requestAttributes);
                task.run();
            } finally {
                SecurityContextHolder.clearContext();
                RequestContextHolder.resetRequestAttributes();
            }
        }, executor);
    }
}
