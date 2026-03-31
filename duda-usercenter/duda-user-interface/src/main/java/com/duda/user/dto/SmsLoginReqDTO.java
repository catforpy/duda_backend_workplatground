package com.duda.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 短信验证码登录请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Data
public class SmsLoginReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String phone;

    private String code;

    private String deviceType;

    private String deviceId;

    private String loginIp;

    private String userAgent;
}
