package com.duda.tenant.mq;

import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.tenant.entity.Tenant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 租户消息生产者
 * 使用duda-common-rocketmq发送租户相关的消息
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
public class TenantMessageProducer {

    @Autowired
    private RocketMQUtils rocketMQUtils;

    /**
     * 租户创建Topic
     */
    private static final String TENANT_CREATE_TOPIC = "tenant-create";

    /**
     * 租户删除Topic
     */
    private static final String TENANT_DELETE_TOPIC = "tenant-delete";

    /**
     * 租户统计Topic
     */
    private static final String TENANT_STATISTICS_TOPIC = "tenant-statistics";

    /**
     * 发送租户创建消息
     *
     * @param tenant 租户信息
     */
    public void sendTenantCreateMessage(Tenant tenant) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("tenantId", tenant.getId());
            message.put("tenantCode", tenant.getTenantCode());
            message.put("tenantName", tenant.getTenantName());
            message.put("tenantType", tenant.getTenantType());
            message.put("packageId", tenant.getPackageId());
            message.put("timestamp", LocalDateTime.now());

            String messageKey = RocketMQUtils.buildMessageKey("tenant-create", tenant.getId());

            rocketMQUtils.syncSendWithKey(TENANT_CREATE_TOPIC, message, messageKey);

            log.info("发送租户创建消息成功: tenantId={}, tenantCode={}, messageKey={}",
                tenant.getId(), tenant.getTenantCode(), messageKey);

        } catch (Exception e) {
            log.error("发送租户创建消息失败: tenantId={}, tenantCode={}",
                tenant.getId(), tenant.getTenantCode(), e);
        }
    }

    /**
     * 发送租户删除消息
     *
     * @param tenantId   租户ID
     * @param tenantCode 租户编码
     */
    public void sendTenantDeleteMessage(Long tenantId, String tenantCode) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("tenantId", tenantId);
            message.put("tenantCode", tenantCode);
            message.put("timestamp", LocalDateTime.now());

            String messageKey = RocketMQUtils.buildMessageKey("tenant-delete", tenantId);

            rocketMQUtils.syncSendWithKey(TENANT_DELETE_TOPIC, message, messageKey);

            log.info("发送租户删除消息成功: tenantId={}, tenantCode={}, messageKey={}",
                tenantId, tenantCode, messageKey);

        } catch (Exception e) {
            log.error("发送租户删除消息失败: tenantId={}, tenantCode={}", tenantId, tenantCode, e);
        }
    }

    /**
     * 发送租户统计消息
     *
     * @param tenantId 租户ID
     * @param statType 统计类型
     * @param statData 统计数据
     */
    public void sendTenantStatisticsMessage(Long tenantId, String statType, Map<String, Object> statData) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("tenantId", tenantId);
            message.put("statType", statType);
            message.put("statData", statData);
            message.put("timestamp", LocalDateTime.now());

            String messageKey = RocketMQUtils.buildMessageKey("tenant-statistics-" + statType, tenantId);

            rocketMQUtils.syncSendWithKey(TENANT_STATISTICS_TOPIC, message, messageKey);

            log.info("发送租户统计消息成功: tenantId={}, statType={}, messageKey={}",
                tenantId, statType, messageKey);

        } catch (Exception e) {
            log.error("发送租户统计消息失败: tenantId={}, statType={}", tenantId, statType, e);
        }
    }
}
