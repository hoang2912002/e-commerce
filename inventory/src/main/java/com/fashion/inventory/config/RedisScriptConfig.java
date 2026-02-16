package com.fashion.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {
    @Bean
    public DefaultRedisScript<Long> scriptDecrementInventory() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/decreaseInventoryQty.lua"));
        script.setResultType(Long.class); // Kết quả trả về từ Lua là số Long
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> scriptIncreaseInventory() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/increaseInventoryQty.lua"));
        script.setResultType(Long.class);
        return script;
    }
}