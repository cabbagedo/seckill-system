package com.seckill.service;

import com.seckill.common.ResultCode;
import com.seckill.dto.SeckillRequest;

public interface SeckillService {
    /**
     * 资格校验 (Redis+Lua 原子) 并投递 Kafka 异步落库.
     * @return SECKILL_QUEUED 表示已入队; 其他即拒绝原因.
     */
    ResultCode trySeckill(SeckillRequest req);

    /** Spring 启动后将 DB 库存预热到 Redis (生产应做开关/定时). */
    void preloadStock(Long productId);
}
