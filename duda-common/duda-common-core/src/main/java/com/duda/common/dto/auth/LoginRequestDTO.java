package com.duda.common.dto.auth;

import com.duda.common.enums.LoginType;
import com.duda.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * 支持3种登录方式：
 * 1. 手机号+密码
 * 2. 邮箱+密码
 * 3. 第三方登录
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@Schema(description = "登录请求")
public class LoginRequestDTO {

    /**
     * 登录方式
     * 必填
     */
    @Schema(description = "登录方式：phone_password-手机号密码, email_password-邮箱密码, third_party-第三方登录",
            required = true,
            example = "phone_password")
    @NotBlank(message = "登录方式不能为空")
    private String loginType;

    /**
     * 用户类型
     * 可选，用于区分不同身份的用户登录
     */
    @Schema(description = "用户类型：normal-普通用户, merchant-商家, operator-运营, admin-管理员",
            example = "normal")
    private String userType;

    /**
     * 手机号
     * login_type=phone_password 时必填
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     * login_type=email_password 时必填
     */
    @Schema(description = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     * login_type=phone_password 或 email_password 时必填
     */
    @Schema(description = "密码", example = "password123")
    private String password;

    /**
     * 第三方平台
     * login_type=third_party 时必填
     */
    @Schema(description = "第三方平台：wechat-微信, qq-QQ, alipay-支付宝, weibo-微博",
            example = "wechat")
    private String thirdPartyPlatform;

    /**
     * 第三方授权码
     * login_type=third_party 时必填
     */
    @Schema(description = "第三方授权码（access_token或auth_code）", example = "auth_code_xxx")
    @NotBlank(message = "第三方授权码不能为空")
    private String thirdPartyAuthCode;

    /**
     * 第三方OpenID
     * login_type=third_party 时可选，用于绑定已存在的账号
     */
    @Schema(description = "第三方OpenID", example = "oXXXXXXXXXXXXXXXX")
    private String thirdPartyOpenId;

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
     * 验证登录参数是否完整
     */
    public void validate() {
        LoginType type = LoginType.fromCode(this.loginType);

        switch (type) {
            case PHONE_PASSWORD:
                if (phone == null || phone.isBlank()) {
                    throw new IllegalArgumentException("手机号不能为空");
                }
                if (password == null || password.isBlank()) {
                    throw new IllegalArgumentException("密码不能为空");
                }
                break;

            case EMAIL_PASSWORD:
                if (email == null || email.isBlank()) {
                    throw new IllegalArgumentException("邮箱不能为空");
                }
                if (password == null || password.isBlank()) {
                    throw new IllegalArgumentException("密码不能为空");
                }
                break;

            case THIRD_PARTY:
                if (thirdPartyPlatform == null || thirdPartyPlatform.isBlank()) {
                    throw new IllegalArgumentException("第三方平台不能为空");
                }
                if (thirdPartyAuthCode == null || thirdPartyAuthCode.isBlank()) {
                    throw new IllegalArgumentException("第三方授权码不能为空");
                }
                break;

            default:
                throw new IllegalArgumentException("不支持的登录方式: " + type);
        }
    }

    /**
     * 获取登录账号（手机号或邮箱）
     */
    public String getAccount() {
        LoginType type = LoginType.fromCode(this.loginType);
        if (type == LoginType.PHONE_PASSWORD) {
            return phone;
        } else if (type == LoginType.EMAIL_PASSWORD) {
            return email;
        }
        return null;
    }
}
