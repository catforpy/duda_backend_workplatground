package com.duda.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机验证码登录请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@Schema(description = "手机验证码登录请求")
public class SmsCodeLoginReqDTO {

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
     * 客户端IP
     * 可选，系统自动获取
     */
    @Schema(description = "客户端IP", example = "192.168.1.1")
    private String clientIp;

    /**
     * 客户端类型
     * 可选
     */
    @Schema(description = "客户端类型：web-web端, app-App端, mini_program-小程序", example = "web")
    private String clientType;

    /**
     * 设备唯一标识
     * 可选
     */
    @Schema(description = "设备唯一标识", example = "device_uuid_xxx")
    private String deviceId;
}
