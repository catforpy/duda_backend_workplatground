package com.duda.user.listener;

import com.duda.common.mq.message.UserCacheMsg;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
@RocketMQMessageListener(
        topic = "UserCacheAsyncDelete",
        consumerGroup = "user-cache-sync-group",
        consumeThreadNumber = 5
)
public class UserCacheChangeListener implements RocketMQListener<UserCacheMsg> {

    private static final Logger logger = LoggerFactory.getLogger(UserCacheChangeListener.class);

    @Override
    public void onMessage(UserCacheMsg message) {
        try {
            logger.info("=== 收到用户缓存变更消息 ===");
            logger.info("用户ID: {}", message.getUserId());
            logger.info("操作类型: {}", message.getOperation());
            logger.info("变更时间: {}", message.getChangeTime());
            logger.info("变更字段: {}", message.getChangedFields());
            logger.info("变更原因: {}", message.getReason());
            logger.info("消息ID: {}", message.getMessageId());
            logger.info("======================");

            // TODO: 清除本服务中关于该用户的所有缓存
            clearUserCache(message.getUserId());

            logger.info("✅ 用户缓存变更消息处理成功！userId={}", message.getUserId());

        } catch (Exception e) {
            logger.error("❌ 处理用户缓存变更消息失败！userId={}, error={}",
                message.getUserId(), e.getMessage(), e);
            throw new RuntimeException("处理缓存变更消息失败", e);
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
