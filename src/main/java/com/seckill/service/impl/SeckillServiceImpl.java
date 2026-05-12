package com.seckill.service.impl;

import com.seckill.common.ResultCode;
import com.seckill.dto.OrderMessage;
import com.seckill.dto.SeckillRequest;
import com.seckill.entity.Stock;
import com.seckill.kafka.OrderProducer;
import com.seckill.service.SeckillService;
import com.seckill.service.StockService;
import com.seckill.util.OrderIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    private static final String STOCK_KEY_PREFIX = "stock:";
    private static final String USER_SET_KEY_PREFIX = "seckill:user:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private DefaultRedisScript<Long> seckillScript;
    @Resource
    private StockService stockService;
    @Resource
    private OrderProducer orderProducer;

    @Override
    public ResultCode trySeckill(SeckillRequest req) {
        String stockKey = STOCK_KEY_PREFIX + req.getProductId();
        String userKey = USER_SET_KEY_PREFIX + req.getProductId();
        List<String> keys = Arrays.asList(stockKey, userKey);

        Long ret = redisTemplate.execute(
                seckillScript, keys,
                String.valueOf(req.getUserId()),
                String.valueOf(req.getQuantity()));

        if (ret == null) {
            return ResultCode.SYSTEM_ERROR;
        }
        switch (ret.intValue()) {
            case -1: return ResultCode.STOCK_NOT_ENOUGH;
            case -2: return ResultCode.DUPLICATE_PURCHASE;
            case -3: return ResultCode.PRODUCT_NOT_FOUND;
        }

        // Redis 已预扣成功 -> 投递 Kafka, 同 productId 进同分区保证顺序
        String orderNo = OrderIdGenerator.next();
        OrderMessage msg = new OrderMessage(orderNo, req.getUserId(), req.getProductId(), req.getQuantity());
        orderProducer.send(String.valueOf(req.getProductId()), msg);
        log.info("seckill queued orderNo={} userId={} productId={}", orderNo, req.getUserId(), req.getProductId());
        return ResultCode.SECKILL_QUEUED;
    }

    @Override
    public void preloadStock(Long productId) {
        Stock s = stockService.getByProductId(productId);
        if (s == null) return;
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + productId, s.getAvailable());
        redisTemplate.delete(USER_SET_KEY_PREFIX + productId);
        log.info("preload stock productId={} available={}", productId, s.getAvailable());
    }
}
