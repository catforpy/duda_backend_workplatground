package com.duda.common.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信验证码校验结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsCheckDTO {

    /**
     * 校验是否通过
     */
    private boolean checkStatus;

    /**
     * 结果描述
     */
    private String desc;
}
