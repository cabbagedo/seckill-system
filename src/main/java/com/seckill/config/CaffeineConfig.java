package com.seckill.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seckill.cache.LogicalExpireData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, LogicalExpireData> productLocalCache() {
        return Caffeine.newBuilder()
                .initialCapacity(64)
                .maximumSize(10_000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
