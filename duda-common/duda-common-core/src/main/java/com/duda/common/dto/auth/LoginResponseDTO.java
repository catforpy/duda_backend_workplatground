package com.duda.common.dto.auth;

import com.duda.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应DTO
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginResponseDTO {

    /**
     * 访问令牌（短token）
     * 有效期：15分钟
     */
    @Schema(description = "访问令牌（Access Token）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * 刷新令牌（长token）
     * 有效期：7天
     */
    @Schema(description = "刷新令牌（Refresh Token）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
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
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "138****8000")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "user***@example.com")
    private String email;

    /**
     * 是否首次登录
     */
    @Schema(description = "是否首次登录", example = "false")
    private Boolean isFirstLogin;

    /**
     * 是否需要完善信息
     */
    @Schema(description = "是否需要完善信息", example = "true")
    private Boolean needCompleteInfo;

    /**
     * 账号状态：active-激活, inactive-未激活, suspended-暂停
     */
    @Schema(description = "账号状态", example = "active")
    private String status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    /**
     * 所属公司ID
     */
    @Schema(description = "所属公司ID", example = "1001")
    private Long companyId;

    /**
     * 部门
     */
    @Schema(description = "部门", example = "技术部")
    private String department;

    /**
     * 职位
     */
    @Schema(description = "职位", example = "工程师")
    private String position;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2026-03-11T10:30:00")
    private String loginTime;
}
