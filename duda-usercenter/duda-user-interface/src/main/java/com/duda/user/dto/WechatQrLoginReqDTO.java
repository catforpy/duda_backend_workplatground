package com.duda.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信扫码登录请求DTO（预留接口）
 */
@Data
@Schema(description = "微信扫码登录请求")
public class WechatQrLoginReqDTO {

    @NotBlank(message = "微信code不能为空")
    @Schema(description = "微信授权code", example = "wx_code_123456")
    private String code;

    @Schema(description = "微信state参数", example = "state_abc")
    private String state;
}
