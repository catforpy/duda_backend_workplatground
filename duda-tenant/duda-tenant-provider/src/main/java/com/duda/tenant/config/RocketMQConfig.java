package com.duda.tenant.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ配置
 * 配置Producer用于发送消息
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name-server:120.26.170.213:9876}")
    private String nameServer;

    @Value("${rocketmq.producer.group:tenant-producer-group}")
    private String producerGroup;

    @Bean
    public DefaultMQProducer producer() throws Exception {
        log.info("初始化RocketMQ Producer: nameServer={}, group={}", nameServer, producerGroup);

        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.setMaxMessageSize(4 * 1024 * 1024);  // 4MB
        producer.setSendMsgTimeout(3000);
        producer.setRetryTimesWhenSendFailed(2);

        producer.start();
        log.info("RocketMQ Producer启动成功");

        return producer;
    }
}
