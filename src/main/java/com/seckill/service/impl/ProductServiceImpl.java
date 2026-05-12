package com.seckill.service.impl;

import com.seckill.cache.ProductCacheService;
import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductCacheService productCacheService;

    @Override
    public Product getDetail(Long productId) {
        return productCacheService.getProduct(productId);
    }
}
