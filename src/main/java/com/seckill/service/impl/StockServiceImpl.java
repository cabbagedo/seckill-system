package com.seckill.service.impl;

import com.seckill.entity.Stock;
import com.seckill.mapper.StockMapper;
import com.seckill.service.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Resource
    private StockMapper stockMapper;

    @Override
    public Stock getByProductId(Long productId) {
        return stockMapper.selectByProductId(productId);
    }

    @Override
    public boolean deductWithRetry(Long productId, int count, int retryTimes) {
        for (int i = 0; i < retryTimes; i++) {
            Stock s = stockMapper.selectByProductId(productId);
            if (s == null || s.getAvailable() < count) {
                return false;
            }
            int affected = stockMapper.deductByOptimisticLock(productId, count, s.getVersion());
            if (affected == 1) {
                return true;
            }
            log.warn("optimistic-lock conflict productId={} attempt={}", productId, i + 1);
        }
        return false;
    }
}
