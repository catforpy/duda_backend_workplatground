package com.duda.common.enums;

/**
 * 登录方式枚举
 *
 * 定义3种登录方式
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public enum LoginType {

    /**
     * 手机号+密码登录
     * - 需要手机号验证
     * - 需要密码验证
     * - 最常用的登录方式
     */
    PHONE_PASSWORD("phone_password", "手机号密码登录", 1),

    /**
     * 邮箱+密码登录
     * - 需要邮箱验证
     * - 需要密码验证
     * - 适合海外用户
     */
    EMAIL_PASSWORD("email_password", "邮箱密码登录", 2),

    /**
     * 第三方登录
     * - 微信、QQ、支付宝等
     * - OAuth 2.0 授权
     * - 无需密码
     */
    THIRD_PARTY("third_party", "第三方登录", 3);

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 类型顺序
     */
    private final Integer order;

    LoginType(String code, String name, Integer order) {
        this.code = code;
        this.name = name;
        this.order = order;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    /**
     * 根据代码获取枚举
     */
    public static LoginType fromCode(String code) {
        for (LoginType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的登录类型: " + code);
    }

    /**
     * 是否需要密码
     */
    public boolean requirePassword() {
        return this == PHONE_PASSWORD || this == EMAIL_PASSWORD;
    }

    /**
     * 是否是第三方登录
     */
    public boolean isThirdParty() {
        return this == THIRD_PARTY;
    }
}
