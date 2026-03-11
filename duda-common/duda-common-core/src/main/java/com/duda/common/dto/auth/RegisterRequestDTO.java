package com.duda.common.dto.auth;

import com.duda.common.enums.UserType;
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
 * 1. 手机号+密码注册
 * 2. 邮箱+密码注册
 * 3. 第三方账号绑定注册
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@Schema(description = "注册请求")
public class RegisterRequestDTO {

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
     */
    @Schema(description = "注册方式：phone-手机号, email-邮箱, third_party-第三方",
            required = true,
            example = "phone")
    @NotBlank(message = "注册方式不能为空")
    private String registerType;

    /**
     * 用户名
     * 可选，3-20个字符
     */
    @Schema(description = "用户名（3-20个字符）", example = "johndoe")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     * register_type=phone 或 email 时必填
     * 6-20个字符，必须包含字母和数字
     */
    @Schema(description = "密码（6-20个字符，必须包含字母和数字）", example = "password123")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

    /**
     * 确认密码
     * register_type=phone 或 email 时必填
     */
    @Schema(description = "确认密码", example = "password123")
    private String confirmPassword;

    /**
     * 手机号
     * register_type=phone 时必填
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 手机验证码
     * register_type=phone 时必填
     */
    @Schema(description = "手机验证码", example = "123456")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String phoneVerifyCode;

    /**
     * 邮箱
     * register_type=email 时必填
     */
    @Schema(description = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 邮箱验证码
     * register_type=email 时必填
     */
    @Schema(description = "邮箱验证码", example = "123456")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String emailVerifyCode;

    /**
     * 第三方平台
     * register_type=third_party 时必填
     */
    @Schema(description = "第三方平台：wechat-微信, qq-QQ, alipay-支付宝",
            example = "wechat")
    private String thirdPartyPlatform;

    /**
     * 第三方OpenID
     * register_type=third_party 时必填
     */
    @Schema(description = "第三方OpenID", example = "oXXXXXXXXXXXXXXXX")
    private String thirdPartyOpenId;

    /**
     * 第三方UnionID（可选）
     */
    @Schema(description = "第三方UnionID（用于同一应用的不同分享）", example = "uXXXXXXXXXXXXXXXX")
    private String thirdPartyUnionId;

    /**
     * 第三方昵称
     * register_type=third_party 时可选
     */
    @Schema(description = "第三方昵称", example = "张三")
    private String thirdPartyNickname;

    /**
     * 第三方头像
     * register_type=third_party 时可选
     */
    @Schema(description = "第三方头像URL", example = "https://example.com/avatar.jpg")
    private String thirdPartyAvatar;

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
        // 验证用户类型
        try {
            UserType.fromCode(this.userType);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的用户类型: " + this.userType);
        }

        // 验证密码一致性
        if (password != null && !password.isBlank()) {
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("两次输入的密码不一致");
            }
        }

        // 根据注册方式验证必填字段
        if ("phone".equals(registerType)) {
            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("手机号不能为空");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("密码不能为空");
            }
            if (phoneVerifyCode == null || phoneVerifyCode.isBlank()) {
                throw new IllegalArgumentException("手机验证码不能为空");
            }
        } else if ("email".equals(registerType)) {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("邮箱不能为空");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("密码不能为空");
            }
            if (emailVerifyCode == null || emailVerifyCode.isBlank()) {
                throw new IllegalArgumentException("邮箱验证码不能为空");
            }
        } else if ("third_party".equals(registerType)) {
            if (thirdPartyPlatform == null || thirdPartyPlatform.isBlank()) {
                throw new IllegalArgumentException("第三方平台不能为空");
            }
            if (thirdPartyOpenId == null || thirdPartyOpenId.isBlank()) {
                throw new IllegalArgumentException("第三方OpenID不能为空");
            }
        } else {
            throw new IllegalArgumentException("不支持的注册方式: " + registerType);
        }
    }

    /**
     * 获取注册账号（手机号或邮箱）
     */
    public String getAccount() {
        if ("phone".equals(registerType)) {
            return phone;
        } else if ("email".equals(registerType)) {
            return email;
        }
        return null;
    }
}
