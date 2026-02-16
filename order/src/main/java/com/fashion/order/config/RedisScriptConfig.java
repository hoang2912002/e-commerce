package com.fashion.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {
    @Bean
    public DefaultRedisScript<Long> scriptDecrementCoupon() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/decreaseCouponStock.lua"));
        script.setResultType(Long.class); // Kết quả trả về từ Lua là số Long
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> scriptIncreaseCoupon() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/increaseCouponStock.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
