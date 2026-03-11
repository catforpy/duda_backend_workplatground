package com.duda.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Token响应DTO
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@Builder
@Schema(description = "Token响应")
public class TokenDTO {

    @Schema(description = "Access Token（短期token，15分钟）")
    private String accessToken;

    @Schema(description = "Refresh Token（长期token，7天）")
    private String refreshToken;

    @Schema(description = "Token类型")
    private String tokenType;

    @Schema(description = "Access Token有效期（秒）")
    private Integer expiresIn;
}
