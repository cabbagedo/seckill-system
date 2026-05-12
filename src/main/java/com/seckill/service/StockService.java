package com.seckill.service;

import com.seckill.entity.Stock;

public interface StockService {
    Stock getByProductId(Long productId);

    /** DB 乐观锁扣减, 最多 retryTimes 次, 返回是否成功. */
    boolean deductWithRetry(Long productId, int count, int retryTimes);
}
