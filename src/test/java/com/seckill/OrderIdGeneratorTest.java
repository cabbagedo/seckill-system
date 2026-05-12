package com.seckill;

import org.junit.jupiter.api.Test;

class OrderIdGeneratorTest {
    @Test
    void next_should_be_unique() {
        java.util.Set<String> set = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            org.junit.jupiter.api.Assertions.assertTrue(set.add(com.seckill.util.OrderIdGenerator.next()));
        }
    }
}
