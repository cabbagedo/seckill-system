package com.seckill.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Value("${seckill.topic.order}")
    private String orderTopic;

    @Bean
    public NewTopic orderTopic() {
        return new NewTopic(orderTopic, 3, (short) 1);
    }
}
