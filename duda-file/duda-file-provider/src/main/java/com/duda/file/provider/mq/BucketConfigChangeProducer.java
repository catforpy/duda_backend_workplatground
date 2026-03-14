package com.duda.file.provider.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Bucket配置变更消息生产者
 * 参考老代码手动发送消息
 *
 * @author duda
 * @date 2026-03-14
 */
@Slf4j
@Component
public class BucketConfigChangeProducer {

    @Autowired
    private DefaultMQProducer producer;

    /**
     * 发送配置变更消息
     *
     * @param bucketName Bucket名称
     * @param configType 配置类型（CORS, REFERER, VERSIONING, WEBSITE, ACCELERATION, ALL）
     * @param action 操作类型（SET, DELETE, SYNC）
     */
    public void sendConfigChangeMessage(String bucketName, String configType, String action) {
        try {
            // 构建消息
            String messageBody = buildMessage(bucketName, configType, action);

            log.info("→ 发送配置变更消息:");
            log.info("  Topic: BUCKET_CONFIG_CHANGE_TOPIC");
            log.info("  Bucket: {}", bucketName);
            log.info("  配置类型: {}", configType);
            log.info("  操作: {}", action);

            // 创建消息
            Message message = new Message();
            message.setTopic("BUCKET_CONFIG_CHANGE_TOPIC");
            message.setBody(messageBody.getBytes());
            message.setKeys("bucket-config-" + bucketName);

            // 发送消息
            producer.send(message);

            log.info("✓ 消息发送成功");

        } catch (Exception e) {
            log.error("✗ 发送消息失败", e);
        }
    }

    /**
     * 发送所有配置变更消息
     *
     * @param bucketName Bucket名称
     */
    public void sendAllConfigChangeMessage(String bucketName) {
        sendConfigChangeMessage(bucketName, "ALL", "SYNC");
    }

    /**
     * 构建消息JSON
     */
    private String buildMessage(String bucketName, String configType, String action) {
        return String.format(
            "{\"bucketName\":\"%s\",\"configType\":\"%s\",\"action\":\"%s\",\"timestamp\":%d}",
            bucketName,
            configType,
            action,
            Instant.now().getEpochSecond()
        );
    }
}
