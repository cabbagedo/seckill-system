package com.seckill.kafka;

import com.seckill.dto.OrderMessage;
import com.seckill.entity.Order;
import com.seckill.entity.Product;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.ProductService;
import com.seckill.service.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderConsumer {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private StockService stockService;
    @Resource
    private ProductService productService;

    /** 批量消费; 单条失败不影响其它; ack 手动提交保证至少一次 */
    @KafkaListener(topics = "${seckill.topic.order}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(List<ConsumerRecord<String, OrderMessage>> records, Acknowledgment ack) {
        for (ConsumerRecord<String, OrderMessage> rec : records) {
            try {
                process(rec.value());
            } catch (Exception e) {
                log.error("consume failed offset={} value={}", rec.offset(), rec.value(), e);
                // 真实生产: 投递到死信 topic; 此处吞掉避免堵塞
            }
        }
        ack.acknowledge();
    }

    @Transactional(rollbackFor = Exception.class)
    public void process(OrderMessage msg) {
        // 幂等: order_no 唯一键; 已存在直接返回
        if (orderMapper.selectByOrderNo(msg.getOrderNo()) != null) {
            log.info("duplicate consume skip orderNo={}", msg.getOrderNo());
            return;
        }

        boolean ok = stockService.deductWithRetry(msg.getProductId(), msg.getQuantity(), 3);
        if (!ok) {
            // Redis 已扣但 DB 扣失败 -> 真实场景需补偿/告警; 此处只记日志
            log.error("DB deduct failed orderNo={} productId={}", msg.getOrderNo(), msg.getProductId());
            return;
        }

        Product p = productService.getDetail(msg.getProductId());
        Order order = new Order();
        order.setOrderNo(msg.getOrderNo());
        order.setUserId(msg.getUserId());
        order.setProductId(msg.getProductId());
        order.setQuantity(msg.getQuantity());
        order.setAmount(p.getPrice().multiply(new java.math.BigDecimal(msg.getQuantity())));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        try {
            orderMapper.insert(order);
            log.info("order persisted orderNo={}", msg.getOrderNo());
        } catch (DuplicateKeyException e) {
            log.info("duplicate insert ignored orderNo={}", msg.getOrderNo());
        }
    }
}
