package com.duda.user.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 短信验证码响应DTO
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Data
@Builder
public class SmsSendCodeRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    private Integer expireSeconds;

    private Boolean success;

    private String message;

    private Integer remainingCount;
}
