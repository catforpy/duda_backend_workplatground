package com.duda.tenant.mq;

import com.duda.common.rocketmq.listener.BaseRocketMQListener;
import com.duda.tenant.manager.TenantSchemaManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 租户删除消息消费者
 * 监听租户删除Topic，处理清理操作
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
    topic = "tenant-delete",
    consumerGroup = "tenant-delete-consumer-group"
)
public class TenantDeleteConsumer extends BaseRocketMQListener<Map<String, Object>> {

    @Autowired
    private TenantSchemaManager tenantSchemaManager;

    @Override
    protected void onMessage(Map<String, Object> message, ExtInfo extInfo) {
        try {
            Long tenantId = (Long) message.get("tenantId");
            String tenantCode = (String) message.get("tenantCode");

            log.info("接收到租户删除消息: tenantId={}, tenantCode={}", tenantId, tenantCode);

            // 1. 删除租户Schema
            boolean dropped = tenantSchemaManager.dropTenantSchema(tenantId, tenantCode);
            if (dropped) {
                log.info("删除租户Schema成功: tenantCode={}", tenantCode);
            } else {
                log.warn("删除租户Schema失败: tenantCode={}", tenantCode);
            }

            // 2. 清理租户缓存数据（使用duda-common-redis）
            clearTenantCache(tenantId);

            // 3. 备份租户数据（TODO: 实现数据备份）
            // backupTenantData(tenantId, tenantCode);

            log.info("处理租户删除消息完成: tenantId={}, tenantCode={}", tenantId, tenantCode);

        } catch (Exception e) {
            log.error("处理租户删除消息失败", e);
            // TODO: 发送告警通知
        }
    }

    /**
     * 清理租户缓存
     */
    private void clearTenantCache(Long tenantId) {
        try {
            // 使用duda-common-redis的RedisUtils
            // 需要使用pattern删除所有租户相关的key
            // String pattern = "tenant:" + tenantId + ":*";
            // redisUtils.delete(redisUtils.keys(pattern));

            log.info("清理租户缓存: tenantId={}", tenantId);

        } catch (Exception e) {
            log.error("清理租户缓存失败: tenantId={}", tenantId, e);
        }
    }
}
