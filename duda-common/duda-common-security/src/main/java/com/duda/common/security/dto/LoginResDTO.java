package com.duda.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录响应DTO
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginResDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "Access Token")
    private String accessToken;

    @Schema(description = "Refresh Token")
    private String refreshToken;

    @Schema(description = "Token类型")
    private String tokenType;

    @Schema(description = "Access Token有效期（秒）")
    private Integer expiresIn;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
