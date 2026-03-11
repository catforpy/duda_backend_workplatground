package com.duda.common.web.enums;

import lombok.Getter;

/**
 * 短信发送结果枚举
 */
@Getter
public enum SmsSendResultEnum {

    /**
     * 发送成功
     */
    SEND_SUCCESS(0, "发送成功"),

    /**
     * 参数错误（手机号格式错误、发送过于频繁等）
     */
    PARAM_ERROR(1, "参数错误"),

    /**
     * 发送失败
     */
    SEND_FAIL(2, "发送失败");

    private final int code;
    private final String desc;

    SmsSendResultEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
