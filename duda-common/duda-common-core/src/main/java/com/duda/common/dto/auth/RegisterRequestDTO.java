package com.duda.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 *
 * 支持3种注册方式：
 * 1. 都达网账号注册：username + password
 * 2. 手机号验证码注册：phone + phoneVerifyCode（无密码）
 * 3. 微信扫码注册：微信授权信息（一次性完成注册和登录）
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@Schema(description = "注册请求")
public class RegisterRequestDTO {

    // ==================== 通用必填字段 ====================

    /**
     * 用户类型
     * 必填，用于区分注册身份
     */
    @Schema(description = "用户类型：normal-普通用户, merchant-商家, operator-运营, admin-管理员",
            required = true,
            example = "normal")
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    /**
     * 注册方式
     * 必填
     * - account：都达网账号注册（username + password）
     * - phone_sms：手机号验证码注册（phone + phoneVerifyCode，无密码）
     * - wechat_scan：微信扫码注册（微信授权信息）
     */
    @Schema(description = "注册方式：account-都达网账号, phone_sms-手机号验证码, wechat_scan-微信扫码",
            required = true,
            example = "account",
            allowableValues = {"account", "phone_sms", "wechat_scan"})
    @NotBlank(message = "注册方式不能为空")
    private String registerType;

    // ==================== 方式1：都达网账号注册字段 ====================

    /**
     * 用户名
     * registerType=account 时必填
     * 3-20个字符，只能包含字母、数字和下划线
     */
    @Schema(description = "用户名（3-20个字符）", example = "johndoe")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     * registerType=account 时必填
     * 6-20个字符，必须包含字母和数字
     */
    @Schema(description = "密码（6-20个字符，必须包含字母和数字）", example = "pass123456")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

    /**
     * 确认密码
     * registerType=account 时必填
     */
    @Schema(description = "确认密码", example = "pass123456")
    private String confirmPassword;

    // ==================== 方式2：手机号验证码注册字段 ====================

    /**
     * 手机号
     * registerType=phone_sms 时必填
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 手机验证码
     * registerType=phone_sms 时必填
     */
    @Schema(description = "手机验证码", example = "1234")
    @Pattern(regexp = "^\\d{4}$", message = "验证码格式不正确")
    private String phoneVerifyCode;

    // ==================== 方式3：微信扫码注册字段 ====================

    /**
     * 第三方平台
     * registerType=wechat_scan 时必填
     */
    @Schema(description = "第三方平台：wechat-微信", example = "wechat")
    private String thirdPartyPlatform;

    /**
     * 第三方OpenID
     * registerType=wechat_scan 时必填
     */
    @Schema(description = "微信OpenID", example = "oXXXXXXXXXXXXXXXX")
    private String thirdPartyOpenId;

    /**
     * 第三方UnionID（可选）
     */
    @Schema(description = "微信UnionID（用于同一应用的不同分享）", example = "uXXXXXXXXXXXXXXXX")
    private String thirdPartyUnionId;

    /**
     * 第三方昵称
     * registerType=wechat_scan 时可选
     */
    @Schema(description = "微信昵称", example = "张三")
    private String thirdPartyNickname;

    /**
     * 第三方头像
     * registerType=wechat_scan 时可选
     */
    @Schema(description = "微信头像URL", example = "https://example.com/avatar.jpg")
    private String thirdPartyAvatar;

    // ==================== 可选字段（所有方式通用）====================

    /**
     * 真实姓名
     * 可选
     */
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    /**
     * 邀请码
     * 可选，用于推荐关系
     */
    @Schema(description = "邀请码", example = "INVITE123")
    private String inviteCode;

    /**
     * 客户端IP
     * 系统自动获取
     */
    @Schema(description = "客户端IP", example = "192.168.1.1")
    private String clientIp;

    /**
     * 客户端类型：web, app, mini_program
     */
    @Schema(description = "客户端类型：web-web端, app-App端, mini_program-小程序",
            example = "web")
    private String clientType;

    /**
     * 设备唯一标识
     */
    @Schema(description = "设备唯一标识", example = "device_uuid_xxx")
    private String deviceId;

    /**
     * 验证注册参数是否完整
     */
    public void validate() {
        // 验证用户类型（支持新旧两种类型）
        // 旧类型：normal, merchant, operator, admin
        // 新类型：platform_account, service_provider, platform_admin, backend_admin
        boolean isValidOldType = "normal".equals(userType) || "merchant".equals(userType)
                || "operator".equals(userType) || "admin".equals(userType);
        boolean isValidNewType = "platform_account".equals(userType)
                || "service_provider".equals(userType)
                || "platform_admin".equals(userType)
                || "backend_admin".equals(userType);

        if (!isValidOldType && !isValidNewType) {
            throw new IllegalArgumentException("无效的用户类型: " + userType);
        }

        // 根据注册方式验证必填字段
        if ("account".equals(registerType)) {
            // 都达网账号注册
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("用户名不能为空");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("密码不能为空");
            }
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("两次输入的密码不一致");
            }
        } else if ("phone_sms".equals(registerType)) {
            // 手机号验证码注册
            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("手机号不能为空");
            }
            if (phoneVerifyCode == null || phoneVerifyCode.isBlank()) {
                throw new IllegalArgumentException("手机验证码不能为空");
            }
        } else if ("wechat_scan".equals(registerType)) {
            // 微信扫码注册
            if (thirdPartyPlatform == null || thirdPartyPlatform.isBlank()) {
                throw new IllegalArgumentException("第三方平台不能为空");
            }
            if (thirdPartyOpenId == null || thirdPartyOpenId.isBlank()) {
                throw new IllegalArgumentException("微信OpenID不能为空");
            }
        } else {
            throw new IllegalArgumentException("不支持的注册方式: " + registerType);
        }
    }

    /**
     * 获取注册账号（用户名或手机号）
     */
    public String getAccount() {
        if ("account".equals(registerType)) {
            return username;
        } else if ("phone_sms".equals(registerType)) {
            return phone;
        }
        return null;
    }
}
