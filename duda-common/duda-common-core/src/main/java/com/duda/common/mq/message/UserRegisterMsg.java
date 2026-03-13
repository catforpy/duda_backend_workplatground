package com.duda.common.mq.message;

import com.duda.common.mq.BaseMqMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户注册消息
 *
 * 用于：
 * - 发送欢迎邮件/短信
 * - 初始化用户数据
 * - 发送新用户优惠券
 * - 风控注册检测
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegisterMsg extends BaseMqMsg {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型（normal/merchant/service_provider/platform_admin/backend_admin）
     */
    private String userType;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 注册时间
     */
    private String registerTime;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 注册方式（account/phone_sms/wechat_scan）
     */
    private String registerType;

    /**
     * 邀请码
     */
    private String inviteCode;
}
