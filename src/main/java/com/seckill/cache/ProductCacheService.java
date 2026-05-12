package com.seckill.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.seckill.entity.Product;
import com.seckill.mapper.ProductMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 商品详情两级缓存:
 *   L1 Caffeine 本地缓存 -> L2 Redis -> DB
 * 防击穿: 逻辑过期 + 异步线程刷新 (物理不过期).
 */
@Slf4j
@Component
public class ProductCacheService {

    private static final String KEY_PREFIX = "product:detail:";

    @Resource
    private Cache<String, LogicalExpireData> productLocalCache;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ProductMapper productMapper;

    @Value("${seckill.cache.logical-expire-seconds:600}")
    private long logicalExpireSeconds;

    private final ExecutorService refreshPool = Executors.newFixedThreadPool(4);

    public Product getProduct(Long productId) {
        String key = KEY_PREFIX + productId;

        // L1
        LogicalExpireData local = productLocalCache.getIfPresent(key);
        if (local != null) {
            if (local.isExpired()) {
                asyncRefresh(key, productId);
            }
            return convert(local.getData());
        }

        // L2
        LogicalExpireData remote = (LogicalExpireData) redisTemplate.opsForValue().get(key);
        if (remote != null) {
            productLocalCache.put(key, remote);
            if (remote.isExpired()) {
                asyncRefresh(key, productId);
            }
            return convert(remote.getData());
        }

        // miss -> 同步回源 + 写两级
        Product p = productMapper.selectById(productId);
        if (p != null) {
            LogicalExpireData wrap = new LogicalExpireData(
                    LocalDateTime.now().plusSeconds(logicalExpireSeconds), p);
            redisTemplate.opsForValue().set(key, wrap);
            productLocalCache.put(key, wrap);
        }
        return p;
    }

    private void asyncRefresh(String key, Long productId) {
        refreshPool.submit(() -> {
            try {
                Product p = productMapper.selectById(productId);
                if (p == null) return;
                LogicalExpireData wrap = new LogicalExpireData(
                        LocalDateTime.now().plusSeconds(logicalExpireSeconds), p);
                redisTemplate.opsForValue().set(key, wrap);
                productLocalCache.put(key, wrap);
                log.debug("logical refresh productId={} done", productId);
            } catch (Exception e) {
                log.error("logical refresh failed productId={}", productId, e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Product convert(Object raw) {
        if (raw instanceof Product p) return p;
        // Jackson default typing 可能反序列化为 LinkedHashMap
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        om.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        return om.convertValue(raw, Product.class);
    }
}
