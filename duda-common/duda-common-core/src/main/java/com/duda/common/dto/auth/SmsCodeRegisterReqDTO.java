package com.duda.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机验证码注册请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@Schema(description = "手机验证码注册请求")
public class SmsCodeRegisterReqDTO {

    /**
     * 手机号
     * 必填
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    /**
     * 验证码
     * 必填，4位数字
     */
    @NotNull(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "验证码格式错误")
    @Schema(description = "验证码", example = "1234", required = true)
    private String smsCode;

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
}
