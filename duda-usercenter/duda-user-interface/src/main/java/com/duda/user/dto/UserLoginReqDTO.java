package com.duda.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Data
public class UserLoginReqDTO implements Serializable {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
