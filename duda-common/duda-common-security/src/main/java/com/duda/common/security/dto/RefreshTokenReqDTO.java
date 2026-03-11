package com.duda.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@Schema(description = "刷新Token请求")
public class RefreshTokenReqDTO {

    @NotBlank(message = "Refresh Token不能为空")
    @Schema(description = "Refresh Token", required = true)
    private String refreshToken;
}
