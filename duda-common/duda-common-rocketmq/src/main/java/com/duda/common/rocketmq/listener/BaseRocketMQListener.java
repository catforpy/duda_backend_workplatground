package com.duda.common.rocketmq.listener;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocketMQ 消费者基类
 * 提供通用的消息处理模板
 *
 * 使用方法：
 * <pre>
 * &#64;Component
 * &#64;RocketMQMessageListener(
 *     topic = MqTopicConstants.USER_REGISTER,
 *     consumerGroup = MqConsumerGroupConstants.USER_REGISTER_GROUP,
 *     messageModel = MessageModel.CLUSTERING
 * )
 * public class UserRegisterListener extends BaseRocketMQListener<UserRegisterMsg> {
 *
 *     &#64;Override
 *     protected void onMessage(UserRegisterMsg message, ExtInfo extInfo) {
 *         // 处理业务逻辑
 *         log.info("收到用户注册消息，userId:{}", message.getUserId());
 *     }
 *
 *     &#64;Override
 *     protected void handleException(Exception e, UserRegisterMsg message, ExtInfo extInfo) {
 *         log.error("处理用户注册消息失败，userId:{}", message.getUserId(), e);
 *     }
 * }
 * </pre>
 *
 * @param <T> 消息类型
 * @author DudaNexus
 * @since 2026-03-10
 */
public abstract class BaseRocketMQListener<T> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 消息处理入口（由RocketMQ自动调用）
     *
     * @param message 消息对象
     */
    public void onMessage(T message) {
        ExtInfo extInfo = new ExtInfo();
        try {
            log.info("开始消费MQ消息，message:{}", message);
            onMessage(message, extInfo);
            log.info("MQ消息消费成功");
        } catch (Exception e) {
            log.error("MQ消息消费异常", e);
            handleException(e, message, extInfo);
            throw e; // 重新抛出异常，触发重试机制
        }
    }

    /**
     * 子类实现具体的消息处理逻辑
     *
     * @param message 消息对象
     * @param extInfo 扩展信息
     */
    protected abstract void onMessage(T message, ExtInfo extInfo);

    /**
     * 异常处理（子类可选择性重写）
     *
     * @param e 异常对象
     * @param message 消息对象
     * @param extInfo 扩展信息
     */
    protected void handleException(Exception e, T message, ExtInfo extInfo) {
        log.error("处理MQ消息异常，message:{}", message, e);
    }

    /**
     * 扩展信息类（用于传递消息的额外信息）
     */
    public static class ExtInfo {
        /**
         * 消息ID（RocketMQ自动生成）
         */
        private String msgId;

        /**
         * 消息Key（发送时设置）
         */
        private String keys;

        /**
         * 消息标签
         */
        private String tags;

        /**
         * 消息队列ID
         */
        private Integer queueId;

        public String getMsgId() {
            return msgId;
        }

        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }

        public String getKeys() {
            return keys;
        }

        public void setKeys(String keys) {
            this.keys = keys;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public Integer getQueueId() {
            return queueId;
        }

        public void setQueueId(Integer queueId) {
            this.queueId = queueId;
        }
    }
}
