package com.duda.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 注册响应DTO
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@Builder
@Schema(description = "注册响应")
public class RegisterResponseDTO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "johndoe")
    private String username;

    /**
     * 用户类型
     */
    @Schema(description = "用户类型：normal-普通用户, merchant-商家, operator-运营, admin-管理员",
            example = "normal")
    private String userType;

    /**
     * 用户类型名称
     */
    @Schema(description = "用户类型名称", example = "普通用户")
    private String userTypeName;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    /**
     * 手机号
     */
    @Schema(description = "手机号（已脱敏）", example = "138****8000")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱（已脱敏）", example = "u***@example.com")
    private String email;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 账号状态
     */
    @Schema(description = "账号状态：active-激活, inactive-未激活",
            example = "active")
    private String status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "激活成功")
    private String statusDesc;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间", example = "2026-03-11T10:30:00")
    private String registerTime;

    /**
     * 是否需要登录
     */
    @Schema(description = "是否需要登录（true-自动登录并返回token）",
            example = "true")
    private Boolean needLogin;

    /**
     * 访问令牌（如果自动登录）
     */
    @Schema(description = "访问令牌（自动登录时返回）",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * 刷新令牌（如果自动登录）
     */
    @Schema(description = "刷新令牌（自动登录时返回）",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    /**
     * Token类型
     */
    @Schema(description = "Token类型", example = "Bearer")
    private String tokenType;

    /**
     * 访问令牌过期时间（秒）
     */
    @Schema(description = "访问令牌过期时间（秒）", example = "900")
    private Long expiresIn;

    /**
     * 欢迎消息
     */
    @Schema(description = "欢迎消息", example = "欢迎加入都达云台！")
    private String welcomeMessage;
}
