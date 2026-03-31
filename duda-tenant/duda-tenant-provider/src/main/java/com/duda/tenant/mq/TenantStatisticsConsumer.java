package com.duda.tenant.mq;

import com.duda.common.rocketmq.listener.BaseRocketMQListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 租户统计消息消费者
 * 监听租户统计Topic，处理统计数据更新
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
    topic = "tenant-statistics",
    consumerGroup = "tenant-statistics-consumer-group"
)
public class TenantStatisticsConsumer extends BaseRocketMQListener<Map<String, Object>> {

    @Override
    protected void onMessage(Map<String, Object> message, ExtInfo extInfo) {
        try {
            Long tenantId = (Long) message.get("tenantId");
            String statType = (String) message.get("statType");
            @SuppressWarnings("unchecked")
            Map<String, Object> statData = (Map<String, Object>) message.get("statData");

            log.info("接收到租户统计消息: tenantId={}, statType={}", tenantId, statType);

            // 根据统计类型处理
            switch (statType) {
                case "user_count":
                    // 更新用户数量统计
                    updateUserCountStatistics(tenantId, statData);
                    break;
                case "storage_usage":
                    // 更新存储使用统计
                    updateStorageStatistics(tenantId, statData);
                    break;
                case "api_calls":
                    // 更新API调用统计
                    updateApiCallStatistics(tenantId, statData);
                    break;
                default:
                    log.warn("未知的统计类型: statType={}", statType);
            }

            log.info("处理租户统计消息完成: tenantId={}, statType={}", tenantId, statType);

        } catch (Exception e) {
            log.error("处理租户统计消息失败", e);
        }
    }

    /**
     * 更新用户数量统计
     */
    private void updateUserCountStatistics(Long tenantId, Map<String, Object> statData) {
        try {
            Integer userCount = (Integer) statData.get("userCount");
            // TODO: 更新tenant_statistics表
            log.info("更新用户数量统计: tenantId={}, userCount={}", tenantId, userCount);

        } catch (Exception e) {
            log.error("更新用户数量统计失败: tenantId={}", tenantId, e);
        }
    }

    /**
     * 更新存储统计
     */
    private void updateStorageStatistics(Long tenantId, Map<String, Object> statData) {
        try {
            Long storageUsed = (Long) statData.get("storageUsed");
            // TODO: 更新tenant_statistics表
            log.info("更新存储统计: tenantId={}, storageUsed={}", tenantId, storageUsed);

        } catch (Exception e) {
            log.error("更新存储统计失败: tenantId={}", tenantId, e);
        }
    }

    /**
     * 更新API调用统计
     */
    private void updateApiCallStatistics(Long tenantId, Map<String, Object> statData) {
        try {
            Integer apiCalls = (Integer) statData.get("apiCalls");
            // TODO: 更新tenant_statistics表
            log.info("更新API调用统计: tenantId={}, apiCalls={}", tenantId, apiCalls);

        } catch (Exception e) {
            log.error("更新API调用统计失败: tenantId={}", tenantId, e);
        }
    }
}
