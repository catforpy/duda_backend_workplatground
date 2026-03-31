package com.duda.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 绑定手机号请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Data
public class BindPhoneReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String phone;

    private String code;

    private String contactLabel;

    private Boolean isPrimary;

    private String ip;
}
