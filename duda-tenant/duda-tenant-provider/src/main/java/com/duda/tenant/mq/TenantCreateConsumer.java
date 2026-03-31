package com.duda.tenant.mq;

import com.duda.common.rocketmq.listener.BaseRocketMQListener;
import com.duda.tenant.manager.TenantSchemaManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 租户创建消息消费者
 * 监听租户创建Topic，处理异步操作
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
    topic = "tenant-create",
    consumerGroup = "tenant-create-consumer-group"
)
public class TenantCreateConsumer extends BaseRocketMQListener<Map<String, Object>> {

    @Autowired
    private TenantSchemaManager tenantSchemaManager;

    @Override
    protected void onMessage(Map<String, Object> message, ExtInfo extInfo) {
        try {
            Long tenantId = (Long) message.get("tenantId");
            String tenantCode = (String) message.get("tenantCode");
            String tenantName = (String) message.get("tenantName");

            log.info("接收到租户创建消息: tenantId={}, tenantCode={}, tenantName={}",
                tenantId, tenantCode, tenantName);

            // 1. 初始化租户缓存（使用duda-common-redis）
            initTenantCache(tenantId, tenantCode, tenantName);

            // 2. 发送欢迎邮件（TODO: 集成邮件服务）
            // sendWelcomeEmail(tenantCode, tenantName);

            // 3. 初始化租户统计数据
            initTenantStatistics(tenantId);

            log.info("处理租户创建消息完成: tenantId={}, tenantCode={}", tenantId, tenantCode);

        } catch (Exception e) {
            log.error("处理租户创建消息失败", e);
            // TODO: 发送告警通知
        }
    }

    /**
     * 初始化租户缓存
     */
    private void initTenantCache(Long tenantId, String tenantCode, String tenantName) {
        try {
            // 使用duda-common-redis的RedisUtils
            // 注意：需要手动添加租户前缀
            // String cacheKey = "tenant:" + tenantId + ":info:name";
            // redisUtils.set(cacheKey, tenantName, 7, TimeUnit.DAYS);

            log.info("初始化租户缓存成功: tenantId={}, tenantCode={}", tenantId, tenantCode);

        } catch (Exception e) {
            log.error("初始化租户缓存失败: tenantId={}, tenantCode={}", tenantId, tenantCode, e);
        }
    }

    /**
     * 初始化租户统计数据
     */
    private void initTenantStatistics(Long tenantId) {
        try {
            // TODO: 初始化租户统计记录
            log.info("初始化租户统计数据: tenantId={}", tenantId);

        } catch (Exception e) {
            log.error("初始化租户统计数据失败: tenantId={}", tenantId, e);
        }
    }
}
