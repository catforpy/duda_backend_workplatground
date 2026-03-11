package com.duda.common.web.enums;

import lombok.Getter;

/**
 * 登录方式枚举
 */
@Getter
public enum LoginTypeEnum {

    /**
     * 账号密码登录
     */
    PASSWORD("password", "账号密码登录"),

    /**
     * 手机验证码登录
     */
    SMS_CODE("sms_code", "手机验证码登录"),

    /**
     * 微信扫码登录
     */
    WECHAT_QR("wechat_qr", "微信扫码登录"),

    /**
     * 微信授权登录
     */
    WECHAT_OAUTH("wechat_oauth", "微信授权登录");

    private final String code;
    private final String desc;

    LoginTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static LoginTypeEnum getByCode(String code) {
        for (LoginTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
