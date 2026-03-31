package com.duda.msg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 短信验证码校验结果DTO
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsgCheckDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 校验是否通过
     */
    private boolean checkStatus;

    /**
     * 结果描述
     */
    private String desc;
}
