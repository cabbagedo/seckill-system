package com.seckill.controller;

import com.seckill.common.Result;
import com.seckill.common.ResultCode;
import com.seckill.dto.SeckillRequest;
import com.seckill.service.SeckillService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @PostMapping("/do")
    public Result<Void> seckill(@Valid @RequestBody SeckillRequest req) {
        ResultCode rc = seckillService.trySeckill(req);
        return rc == ResultCode.SECKILL_QUEUED ? Result.success(null) : Result.fail(rc);
    }
}
