package com.duda.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送短信验证码请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Data
public class SmsSendCodeReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String phone;

    private String scene;

    private String deviceType;

    private String deviceId;

    private String ip;
}
