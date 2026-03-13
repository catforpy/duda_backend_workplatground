package com.duda.msg.enums;

import lombok.Getter;

/**
 * 短信发送结果枚举
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Getter
public enum MsgSendResultEnum {

    SEND_SUCCESS(0, "成功"),
    SEND_FAIL(1, "发送失败"),
    MSG_PARAM_ERROR(2, "消息参数异常");

    private final int code;
    private final String desc;

    MsgSendResultEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
