package com.duda.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手机验证码登录请求DTO
 */
@Data
@Schema(description = "手机验证码登录请求")
public class SmsCodeLoginReqDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @NotNull(message = "验证码不能为空")
    @Schema(description = "验证码", example = "1234")
    private Integer code;
}
