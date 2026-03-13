package com.duda.common.mq.message;

import com.duda.common.mq.BaseMqMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户登录消息
 *
 * 用于：
 * - 记录登录日志（安全审计）
 * - 更新用户最后登录时间
 * - 触发推荐算法更新
 * - 风控检测
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserLoginMsg extends BaseMqMsg {

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
     * 登录时间
     */
    private String loginTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 客户端类型（web/ios/android/miniprogram）
     */
    private String clientType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 是否首次登录
     */
    private Boolean isFirstLogin;

    /**
     * 登录方式（account_password/phone_sms/wechat_scan）
     */
    private String loginType;
}
