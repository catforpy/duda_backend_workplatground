package com.duda.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 账号密码登录请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@Schema(description = "账号密码登录请求")
public class PasswordLoginReqDTO {

    /**
     * 用户名
     * 必填
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "testuser", required = true)
    private String username;

    /**
     * 密码
     * 必填
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "pass123456", required = true)
    private String password;

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
