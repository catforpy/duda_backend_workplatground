package com.duda.common.rocketmq;

import com.alibaba.fastjson2.JSON;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RocketMQ 工具类
 * 封装常用的 MQ 操作（直接使用 DefaultMQProducer）
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
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Component
public class RocketMQUtils {

    @Autowired
    private DefaultMQProducer producer;

    /**
     * 同步发送消息（不带Key）
     *
     * @param topic Topic
     * @param message 消息内容
     */
    public <T> void syncSend(String topic, T message) {
        try {
            String messageBody = JSON.toJSONString(message);
            Message msg = new Message(topic, messageBody.getBytes());
            producer.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("发送MQ消息失败，topic: " + topic, e);
        }
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
            String messageBody = JSON.toJSONString(message);
            Message msg = new Message(topic, messageBody.getBytes());
            msg.setKeys(messageKey);
            producer.send(msg);
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
        try {
            String messageBody = JSON.toJSONString(message);
            Message msg = new Message(topic, messageBody.getBytes());
            producer.send(msg, timeout);
        } catch (Exception e) {
            throw new RuntimeException("发送MQ消息失败，topic: " + topic, e);
        }
    }

    /**
     * 异步发送消息（带Key）
     *
     * @param topic Topic
     * @param message 消息内容
     * @param messageKey 消息唯一标识
     */
    public <T> void asyncSendWithKey(String topic, T message, String messageKey) {
        try {
            String messageBody = JSON.toJSONString(message);
            Message msg = new Message(topic, messageBody.getBytes());
            msg.setKeys(messageKey);
            // 异步发送，不关心结果
            producer.send(msg, new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                    // 发送成功，忽略
                }
                @Override
                public void onException(Throwable e) {
                    // 发送失败，记录日志
                    System.err.println("异步发送MQ消息失败，topic: " + topic + ", messageKey: " + messageKey);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("异步发送MQ消息失败，topic: " + topic + ", messageKey: " + messageKey, e);
        }
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
