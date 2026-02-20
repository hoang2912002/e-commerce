package com.fashion.shipping.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Số lượng thread chạy thường trực
        executor.setMaxPoolSize(10); // Tối đa khi hàng chờ đầy
        executor.setQueueCapacity(500); // Hàng chờ cho các event
        executor.setThreadNamePrefix("Product-Service-Async-");
        executor.initialize();
        return executor;
    }
}
