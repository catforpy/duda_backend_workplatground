package com.duda.user.listener;

import com.duda.common.mq.message.UserLoginMsg;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用户登录消息监听器
 *
 * 功能：
 * - 记录登录日志（安全审计）
 * - 更新用户最后登录时间
 * - 风控检测
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Component
@RocketMQMessageListener(
        topic = "UserLogin",
        consumerGroup = "user-login-log-group",
        consumeThreadNumber = 5
)
public class UserLoginListener implements RocketMQListener<UserLoginMsg> {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginListener.class);

    @Override
    public void onMessage(UserLoginMsg message) {
        try {
            logger.info("=== 收到用户登录消息 ===");
            logger.info("用户ID: {}", message.getUserId());
            logger.info("用户名: {}", message.getUsername());
            logger.info("用户类型: {}", message.getUserType());
            logger.info("登录时间: {}", message.getLoginTime());
            logger.info("登录IP: {}", message.getLoginIp());
            logger.info("客户端类型: {}", message.getClientType());
            logger.info("设备ID: {}", message.getDeviceId());
            logger.info("是否首次登录: {}", message.getIsFirstLogin());
            logger.info("登录方式: {}", message.getLoginType());
            logger.info("消息ID: {}", message.getMessageId());
            logger.info("======================");

            // TODO: 实际业务处理
            // 1. 记录登录日志到数据库（user_login_logs 表）
            // 2. 风控检测（检查异常登录、异地登录等）
            // 3. 更新推荐算法（根据登录行为调整推荐）

            // 示例：记录到数据库
            // UserLoginLogPO logPO = new UserLoginLogPO();
            // logPO.setUserId(message.getUserId());
            // logPO.setUsername(message.getUsername());
            // logPO.setLoginIp(message.getLoginIp());
            // logPO.setLoginTime(message.getLoginTime());
            // logPO.setClientType(message.getClientType());
            // logPO.setDeviceId(message.getDeviceId());
            // userLoginLogMapper.insert(logPO);

            // 示例：风控检测
            if (isAbnormalLogin(message)) {
                logger.warn("⚠️ 检测到异常登录！userId={}, ip={}",
                    message.getUserId(), message.getLoginIp());
                // TODO: 发送安全警告邮件/短信
            }

            logger.info("✅ 用户登录消息处理成功！userId={}", message.getUserId());

        } catch (Exception e) {
            logger.error("❌ 处理用户登录消息失败！userId={}, error={}",
                message.getUserId(), e.getMessage(), e);
            // 注意：这里抛出异常会让 MQ 重试
            throw new RuntimeException("处理登录消息失败", e);
        }
    }

    /**
     * 检测是否为异常登录
     *
     * @param message 登录消息
     * @return true=异常, false=正常
     */
    private boolean isAbnormalLogin(UserLoginMsg message) {
        // TODO: 实现风控规则
        // 1. 检查是否为异地登录（与上次登录IP距离过远）
        // 2. 检查登录频率（短时间内多次登录）
        // 3. 检查设备指纹（新设备登录）
        // 4. 检查IP黑名单

        // 示例：简单的规则判断
        // if (message.getIsFirstLogin() && "unknown".equals(message.getClientType())) {
        //     return true; // 首次登录且客户端类型未知
        // }

        return false; // 暂时返回 false
    }
}
