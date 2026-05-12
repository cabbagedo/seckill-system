package com.seckill.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/** 简易订单号: 日期 + 自增 + 随机后缀, 单机够用; 生产建议雪花/Leaf. */
public class OrderIdGenerator {
    private static final AtomicLong SEQ = new AtomicLong(0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String next() {
        return FMT.format(LocalDate.now())
                + String.format("%08d", SEQ.incrementAndGet() % 100_000_000)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
    }
}
