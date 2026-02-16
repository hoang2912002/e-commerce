package com.fashion.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Configuration
public class RedisScript {
    @Bean
    public DefaultRedisScript<Long> scriptDecrementPromotion() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/decreasePromotionQty.lua"));
        script.setResultType(Long.class); // Kết quả trả về từ Lua là số Long
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> scriptIncreasePromotion() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/increasePromotionQty.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
