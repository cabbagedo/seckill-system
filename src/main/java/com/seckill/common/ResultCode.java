package com.seckill.common;

public enum ResultCode {
    SUCCESS(0, "success"),
    SECKILL_QUEUED(2000, "排队中,稍后查询订单"),
    STOCK_NOT_ENOUGH(4001, "库存不足"),
    DUPLICATE_PURCHASE(4002, "请勿重复抢购"),
    PRODUCT_NOT_FOUND(4003, "商品不存在或未预热"),
    PARAM_ERROR(4000, "参数错误"),
    SYSTEM_ERROR(5000, "系统繁忙,请稍后重试");

    public final int code;
    public final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
