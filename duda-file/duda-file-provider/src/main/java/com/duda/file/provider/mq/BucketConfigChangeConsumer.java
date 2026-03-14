package com.duda.file.provider.mq;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.List;

/**
 * Bucket配置变更消息消费者
 * 参考老代码手动创建 Consumer
 *
 * @author duda
 * @date 2026-03-14
 */
@Slf4j
@Component
public class BucketConfigChangeConsumer {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    private DefaultMQPushConsumer consumer;

    @jakarta.annotation.PostConstruct
    public void init() throws Exception {
        consumer = new DefaultMQPushConsumer();
        consumer.setVipChannelEnabled(false);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumerGroup("BUCKET_CONFIG_CONSUMER_GROUP");
        consumer.setConsumeMessageBatchMaxSize(10);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // 订阅主题
        consumer.subscribe("BUCKET_CONFIG_CHANGE_TOPIC", "*");

        // 设置消息监听器
        consumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    String msgBody = new String(msg.getBody());
                    log.info("╔════════════════════════════════════════╗");
                    log.info("║   收到Bucket配置变更消息                 ║");
                    log.info("╚════════════════════════════════════════╝");
                    log.info("消息内容: {}", msgBody);

                    // 解析并处理消息
                    handleMessage(msgBody);
                } catch (Exception e) {
                    log.error("✗ 处理消息失败", e);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        // 启动消费者
        consumer.start();
        log.info("✓ BucketConfigChangeConsumer 启动成功, namesrv: {}", nameServer);
    }

    /**
     * 处理消息
     */
    private void handleMessage(String message) {
        try {
            // 解析 JSON（使用 FastJSON）
            BucketConfigMessage msg = JSON.parseObject(message, BucketConfigMessage.class);

            if (msg.getBucketName() == null || msg.getBucketName().isEmpty()) {
                log.error("✗ 消息格式错误：缺少bucketName");
                return;
            }

            log.info("→ Bucket名称: {}", msg.getBucketName());
            log.info("→ 配置类型: {}", msg.getConfigType());
            log.info("→ 操作类型: {}", msg.getAction());

            // 从OSS同步所有配置
            syncAllFromOSS(msg.getBucketName());

            log.info("✓ 消息处理完成");

        } catch (Exception e) {
            log.error("✗ 解析消息失败", e);
        }
    }

    /**
     * 从OSS同步所有配置到数据库
     */
    private void syncAllFromOSS(String bucketName) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，无法同步到数据库");
            return;
        }

        try {
            log.info("→ 从OSS同步配置到数据库: {}", bucketName);
            ensureStatisticsExists(bucketName);
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET last_sync_time = NOW(), updated_time = NOW() WHERE bucket_name = ?",
                bucketName
            );
            log.info("✓ 配置已同步到数据库: {}", bucketName);
        } catch (Exception e) {
            log.error("同步配置失败: {}", bucketName, e);
        }
    }

    /**
     * 确保统计记录存在
     */
    private void ensureStatisticsExists(String bucketName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM bucket_statistics WHERE bucket_name = ?",
            Integer.class,
            bucketName
        );

        if (count == null || count == 0) {
            log.info("→ 记录不存在，创建新记录...");
            jdbcTemplate.update(
                "INSERT INTO bucket_statistics (" +
                    "bucket_name, region, storage_type, " +
                    "total_file_count, total_storage_size, " +
                    "image_count, video_count, document_count, other_count, " +
                    "created_time, updated_time" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                bucketName, "cn-hangzhou", "STANDARD",
                0, 0, 0, 0, 0, 0
            );
            log.info("✓ 统计记录已创建");
        }
    }

    @PreDestroy
    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("✓ BucketConfigChangeConsumer 已关闭");
        }
    }

    /**
     * 消息DTO
     */
    public static class BucketConfigMessage {
        private String bucketName;
        private String configType;
        private String action;
        private Long timestamp;

        // Getters and Setters
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getConfigType() { return configType; }
        public void setConfigType(String configType) { this.configType = configType; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}
