package com.duda.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更换主手机号请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Data
public class ReplacePrimaryPhoneReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String oldPhone;

    private String newPhone;

    private String oldCode;

    private String newCode;

    private String reason;

    private String ip;
}
