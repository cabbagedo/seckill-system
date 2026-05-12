package com.seckill.controller;

import com.seckill.common.Result;
import com.seckill.entity.Order;
import com.seckill.mapper.OrderMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderMapper orderMapper;

    @GetMapping("/{orderNo}")
    public Result<Order> get(@PathVariable String orderNo) {
        return Result.success(orderMapper.selectByOrderNo(orderNo));
    }
}
