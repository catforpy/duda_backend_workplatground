package com.duda.user.enums;

import lombok.Getter;

/**
 * 短信验证码状态枚举
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Getter
public enum SmsStatusEnum {

    /**
     * 未使用
     */
    UNUSED(0, "未使用"),

    /**
     * 已使用
     */
    USED(1, "已使用"),

    /**
     * 已过期
     */
    EXPIRED(2, "已过期");

    private final Integer code;
    private final String desc;

    SmsStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static SmsStatusEnum fromCode(Integer code) {
        for (SmsStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("无效的状态码: " + code);
    }
}
