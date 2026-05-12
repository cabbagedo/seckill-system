package com.seckill.controller;

import com.seckill.common.Result;
import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import com.seckill.service.SeckillService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource
    private ProductService productService;
    @Resource
    private SeckillService seckillService;

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    /** 手动触发库存预热到 Redis (Demo 用; 生产应是后台任务) */
    @GetMapping("/preload/{id}")
    public Result<Void> preload(@PathVariable Long id) {
        seckillService.preloadStock(id);
        return Result.success(null);
    }
}
