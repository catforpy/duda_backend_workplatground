package com.duda.common.rocketmq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RocketMQ 工具类
 * 封装常用的 MQ 操作
 *
 * 使用方法：
 * <pre>
 * &#64;Autowired
 * private RocketMQUtils rocketMQUtils;
 *
 * // 同步发送消息
 * rocketMQUtils.syncSend(MqTopicConstants.USER_REGISTER, userDTO);
 *
 * // 带Key的同步发送（推荐，便于追踪）
 * rocketMQUtils.syncSendWithKey(MqTopicConstants.USER_REGISTER, userDTO, "user-register-" + userId);
 *
 * // 延时消息（30分钟后）
 * rocketMQUtils.syncSendDelay(MqTopicConstants.ORDER_TIMEOUT, orderId, 16);
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Component
public class RocketMQUtils {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送消息（不带Key）
     *
     * @param topic Topic
     * @param message 消息内容
     */
    public <T> void syncSend(String topic, T message) {
        rocketMQTemplate.syncSend(topic, message);
    }

    /**
     * 同步发送消息（带Key，推荐使用）
     * Key用于消息追踪和排查问题
     *
     * @param topic Topic
     * @param message 消息内容
     * @param messageKey 消息唯一标识（建议格式：{业务}-{操作}-{业务ID}-{时间戳}）
     *                  例如：user-register-123456-1678901234567
     */
    public <T> void syncSendWithKey(String topic, T message, String messageKey) {
        try {
            // 构建消息，设置Key
            Message<T> msg = MessageBuilder.withPayload(message)
                    .setHeader("keys", messageKey)
                    .build();
            rocketMQTemplate.syncSend(topic, msg);
        } catch (Exception e) {
            throw new RuntimeException("发送MQ消息失败，topic: " + topic + ", messageKey: " + messageKey, e);
        }
    }

    /**
     * 同步发送消息（带超时）
     *
     * @param topic Topic
     * @param message 消息内容
     * @param timeout 超时时间（毫秒）
     */
    public <T> void syncSend(String topic, T message, long timeout) {
        rocketMQTemplate.syncSend(topic, message, timeout);
    }

    /**
     * 同步发送消息（带延时）
     *
     * @param topic Topic
     * @param message 消息内容
     * @param timeout 超时时间（毫秒）
     * @param delayLevel 延时等级（1-18，对应 1s-2h）
     */
    public <T> void syncSend(String topic, T message, long timeout, int delayLevel) {
        Message<T> msg = MessageBuilder.withPayload(message).build();
        rocketMQTemplate.syncSend(topic, msg, timeout, delayLevel);
    }

    /**
     * 同步发送延时消息（默认超时3000ms，带Key）
     *
     * @param topic Topic
     * @param message 消息内容
     * @param delayLevel 延时等级（1-18，对应 1s-2h）
     * @param messageKey 消息唯一标识
     */
    public <T> void syncSendDelayWithKey(String topic, T message, int delayLevel, String messageKey) {
        try {
            Message<T> msg = MessageBuilder.withPayload(message)
                    .setHeader("keys", messageKey)
                    .build();
            rocketMQTemplate.syncSend(topic, msg, 3000, delayLevel);
        } catch (Exception e) {
            throw new RuntimeException("发送延时MQ消息失败，topic: " + topic + ", messageKey: " + messageKey, e);
        }
    }

    /**
     * 同步发送延时消息（默认超时3000ms）
     *
     * @param topic Topic
     * @param message 消息内容
     * @param delayLevel 延时等级（1-18，对应 1s-2h）
     */
    public <T> void syncSendDelay(String topic, T message, int delayLevel) {
        Message<T> msg = MessageBuilder.withPayload(message).build();
        rocketMQTemplate.syncSend(topic, msg, 3000, delayLevel);
    }

    /**
     * 异步发送消息
     *
     * @param topic Topic
     * @param message 消息内容
     */
    public <T> void asyncSend(String topic, T message) {
        rocketMQTemplate.asyncSend(topic, message, null);
    }

    /**
     * 异步发送消息（带Key）
     *
     * @param topic Topic
     * @param message 消息内容
     * @param messageKey 消息唯一标识
     */
    public <T> void asyncSendWithKey(String topic, T message, String messageKey) {
        Message<T> msg = MessageBuilder.withPayload(message)
                .setHeader("keys", messageKey)
                .build();
        rocketMQTemplate.asyncSend(topic, msg, null);
    }

    /**
     * 异步发送消息（带超时）
     *
     * @param topic Topic
     * @param message 消息内容
     * @param timeout 超时时间（毫秒）
     */
    public <T> void asyncSend(String topic, T message, long timeout) {
        rocketMQTemplate.asyncSend(topic, message, null, timeout);
    }

    /**
     * 同步发送消息（OneWay，不关心发送结果）
     *
     * @param topic Topic
     * @param message 消息内容
     */
    public <T> void sendOneWay(String topic, T message) {
        rocketMQTemplate.sendOneWay(topic, message);
    }

    /**
     * 生成消息Key（使用UUID，确保全局唯一）
     * 建议格式：{业务前缀}-{业务ID}
     * 例如：buildMessageKey("user-register", userId) => "user-register-123456-uuid"
     *
     * @param businessPrefix 业务前缀（如 user-register, order-create）
     * @param businessId 业务ID（如 userId, orderId）
     * @return 消息Key
     */
    public static String buildMessageKey(String businessPrefix, Object businessId) {
        return businessPrefix + "-" + businessId + "-" + UUID.randomUUID();
    }

    /**
     * 生成消息Key（简化版，只包含业务前缀和UUID）
     *
     * @param businessPrefix 业务前缀
     * @return 消息Key
     */
    public static String buildMessageKey(String businessPrefix) {
        return businessPrefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID();
    }
}
