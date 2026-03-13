package com.duda.user.listener;

import com.duda.common.mq.message.UserRegisterMsg;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用户注册消息监听器
 *
 * 功能：
 * - 发送欢迎邮件/短信
 * - 初始化用户数据
 * - 发送新用户优惠券
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Component
@RocketMQMessageListener(
        topic = "UserRegister",
        consumerGroup = "user-register-welcome-group",
        consumeThreadNumber = 5
)
public class UserRegisterListener implements RocketMQListener<UserRegisterMsg> {

    private static final Logger logger = LoggerFactory.getLogger(UserRegisterListener.class);

    @Override
    public void onMessage(UserRegisterMsg message) {
        try {
            logger.info("=== 收到用户注册消息 ===");
            logger.info("用户ID: {}", message.getUserId());
            logger.info("用户名: {}", message.getUsername());
            logger.info("用户类型: {}", message.getUserType());
            logger.info("真实姓名: {}", message.getRealName());
            logger.info("手机号: {}", message.getPhone());
            logger.info("邮箱: {}", message.getEmail());
            logger.info("注册时间: {}", message.getRegisterTime());
            logger.info("注册IP: {}", message.getRegisterIp());
            logger.info("注册方式: {}", message.getRegisterType());
            logger.info("邀请码: {}", message.getInviteCode());
            logger.info("消息ID: {}", message.getMessageId());
            logger.info("======================");

            // 1. 发送欢迎短信
            sendWelcomeSms(message);

            // 2. 发送欢迎邮件
            sendWelcomeEmail(message);

            // 3. 发送新用户优惠券
            sendNewUserCoupons(message);

            // 4. 初始化用户数据
            initUserData(message);

            logger.info("✅ 用户注册消息处理成功！userId={}", message.getUserId());

        } catch (Exception e) {
            logger.error("❌ 处理用户注册消息失败！userId={}, error={}",
                message.getUserId(), e.getMessage(), e);
            throw new RuntimeException("处理注册消息失败", e);
        }
    }

    /**
     * 发送欢迎短信
     */
    private void sendWelcomeSms(UserRegisterMsg message) {
        if (message.getPhone() == null || message.getPhone().isEmpty()) {
            logger.info("用户未绑定手机号，跳过欢迎短信");
            return;
        }

        try {
            logger.info("📱 发送欢迎短信到: {}", maskPhone(message.getPhone()));

            // TODO: 调用短信服务 RPC
            // SmsReqDTO smsReq = new SmsReqDTO();
            // smsReq.setPhone(message.getPhone());
            // smsReq.setTemplateCode("WELCOME_TEMPLATE");
            // smsReq.setParams(Map.of(
            //     "username", message.getUsername(),
            //     "userId", message.getUserId().toString()
            // ));
            // smsRpc.sendSms(smsReq);

            logger.info("✅ 欢迎短信发送成功");
        } catch (Exception e) {
            logger.error("❌ 欢迎短信发送失败: {}", e.getMessage());
            // 不抛出异常，继续处理其他逻辑
        }
    }

    /**
     * 发送欢迎邮件
     */
    private void sendWelcomeEmail(UserRegisterMsg message) {
        if (message.getEmail() == null || message.getEmail().isEmpty()) {
            logger.info("用户未绑定邮箱，跳过欢迎邮件");
            return;
        }

        try {
            logger.info("📧 发送欢迎邮件到: {}", maskEmail(message.getEmail()));

            // TODO: 调用邮件服务 RPC
            // EmailReqDTO emailReq = new EmailReqDTO();
            // emailReq.setTo(message.getEmail());
            // emailReq.setSubject("欢迎加入都达云台");
            // emailReq.setContent("欢迎 " + message.getUsername() + " 加入都达云台！");
            // emailRpc.sendEmail(emailReq);

            logger.info("✅ 欢迎邮件发送成功");
        } catch (Exception e) {
            logger.error("❌ 欢迎邮件发送失败: {}", e.getMessage());
            // 不抛出异常，继续处理其他逻辑
        }
    }

    /**
     * 发送新用户优惠券
     */
    private void sendNewUserCoupons(UserRegisterMsg message) {
        try {
            logger.info("🎁 发放新用户优惠券给用户: {}", message.getUserId());

            // TODO: 调用优惠券服务 RPC
            // CouponRpc couponRpc = ...;
            // couponRpc.grantNewUserCoupons(message.getUserId());

            logger.info("✅ 新用户优惠券发放成功");
        } catch (Exception e) {
            logger.error("❌ 新用户优惠券发放失败: {}", e.getMessage());
            // 不抛出异常，继续处理其他逻辑
        }
    }

    /**
     * 初始化用户数据
     */
    private void initUserData(UserRegisterMsg message) {
        try {
            logger.info("🔧 初始化用户数据: {}", message.getUserId());

            // TODO: 初始化用户相关数据
            // 1. 创建用户默认文件夹（文件服务）
            // 2. 创建用户默认资料（社交服务）
            // 3. 创建用户购物车（电商服务）

            logger.info("✅ 用户数据初始化成功");
        } catch (Exception e) {
            logger.error("❌ 用户数据初始化失败: {}", e.getMessage());
            // 不抛出异常，继续处理其他逻辑
        }
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 3) {
            return username.charAt(0) + "***@" + parts[1];
        }
        return username.substring(0, 1) + "***" + username.substring(username.length() - 1) + "@" + parts[1];
    }
}
