package com.duda.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 账号密码注册请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@Schema(description = "账号密码注册请求")
public class PasswordRegisterReqDTO {

    /**
     * 用户名
     * 必填，4-20位，只能包含字母、数字、下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含字母、数字、下划线，长度4-20位")
    @Schema(description = "用户名", example = "testuser", required = true)
    private String username;

    /**
     * 密码
     * 必填，6-20位，必须包含字母和数字
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$", message = "密码必须包含字母和数字，长度6-20位")
    @Schema(description = "密码", example = "pass123456", required = true)
    private String password;

    /**
     * 确认密码
     * 必填，需要与密码一致
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "pass123456", required = true)
    private String confirmPassword;

    /**
     * 真实姓名
     * 可选
     */
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    /**
     * 手机号
     * 可选，用于后续身份绑定
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 邮箱
     * 可选，用于后续身份绑定
     */
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    /**
     * 邀请码
     * 可选，用于推荐关系
     */
    @Schema(description = "邀请码", example = "INVITE123")
    private String inviteCode;
}
