package com.seckill.kafka;

import com.seckill.dto.OrderMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderProducer {

    @Value("${seckill.topic.order}")
    private String topic;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    /** key=productId 保证同一商品的扣减顺序 */
    public void send(String key, OrderMessage msg) {
        kafkaTemplate.send(topic, key, msg)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("kafka send failed orderNo={}", msg.getOrderNo(), ex);
                    }
                });
    }
}
