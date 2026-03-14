package com.duda.user.listener;

import com.alibaba.fastjson2.JSON;
import com.duda.common.mq.message.UserCacheMsg;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户缓存变更消息监听器
 *
 * 功能：
 * - 清除其他服务中关于该用户的缓存
 * - 保持各服务数据一致性
 *
 * 注意：这是示例监听器，展示如何在其他服务中监听缓存变更消息
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Component
public class UserCacheChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(UserCacheChangeListener.class);

    @Value("${rocketmq.name-server}")
    private String nameServer;

    private DefaultMQPushConsumer consumer;

    @PostConstruct
    public void init() throws Exception {
        consumer = new DefaultMQPushConsumer();
        consumer.setVipChannelEnabled(false);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumerGroup("user-cache-sync-group");
        consumer.setConsumeMessageBatchMaxSize(10);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // 订阅主题
        consumer.subscribe("UserCacheAsyncDelete", "*");

        // 设置消息监听器
        consumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(
                    List<MessageExt> msgs,
                    ConsumeConcurrentlyContext context) {

                for (MessageExt msg : msgs) {
                    try {
                        String msgBody = new String(msg.getBody());
                        UserCacheMsg message = JSON.parseObject(msgBody, UserCacheMsg.class);

                        logger.info("╔════════════════════════════════════════╗");
                        logger.info("║   收到用户缓存变更消息                     ║");
                        logger.info("╚════════════════════════════════════════╝");
                        logger.info("用户ID: {}", message.getUserId());
                        logger.info("操作类型: {}", message.getOperation());
                        logger.info("变更时间: {}", message.getChangeTime());
                        logger.info("变更字段: {}", message.getChangedFields());
                        logger.info("变更原因: {}", message.getReason());

                        // TODO: 清除本服务中关于该用户的所有缓存
                        clearUserCache(message.getUserId());

                        logger.info("✓ 用户缓存变更消息处理成功！userId={}", message.getUserId());

                    } catch (Exception e) {
                        logger.error("✗ 处理用户缓存变更消息失败", e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // 启动消费者
        consumer.start();
        System.out.println();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   UserCacheChangeListener 启动成功        ║");
        System.out.println("║   Topic: UserCacheAsyncDelete            ║");
        System.out.println("║   Group: user-cache-sync-group           ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
    }

    @PreDestroy
    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
            logger.info("✓ UserCacheChangeListener 已关闭");
        }
    }

    /**
     * 清除用户相关的所有缓存
     *
     * @param userId 用户ID
     */
    private void clearUserCache(Long userId) {
        logger.info("🧹 清除用户相关缓存: userId={}", userId);

        // TODO: 清除各类缓存
        // 1. 用户基本信息缓存
        // String userInfoKey = "duda:user:info:" + userId;
        // redisUtils.delete(userInfoKey);

        // 2. 用户资料缓存
        // String userProfileKey = "duda:user:profile:" + userId;
        // redisUtils.delete(userProfileKey);

        // 3. 用户关系缓存（粉丝、关注列表）
        // String followersKey = "duda:social:followers:" + userId;
        // String followingKey = "duda:social:following:" + userId;
        // redisUtils.delete(followersKey);
        // redisUtils.delete(followingKey);

        // 4. 用户动态列表缓存
        // String postsKey = "duda:social:posts:" + userId;
        // redisUtils.delete(postsKey);

        // 5. 用户订单列表缓存（如果有订单服务）
        // String ordersKey = "duda:order:user:" + userId;
        // redisUtils.delete(ordersKey);

        logger.info("✅ 用户缓存清除完成: userId={}", userId);
    }
}
