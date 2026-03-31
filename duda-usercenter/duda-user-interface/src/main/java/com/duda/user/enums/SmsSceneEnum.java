package com.duda.user.enums;

import lombok.Getter;

/**
 * 短信验证码场景枚举
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Getter
public enum SmsSceneEnum {

    /**
     * 登录
     */
    LOGIN("login", "登录", "您的登录验证码是: %s, 5分钟内有效。"),

    /**
     * 注册
     */
    REGISTER("register", "注册", "您的注册验证码是: %s, 5分钟内有效。"),

    /**
     * 重置密码
     */
    RESET_PWD("reset_pwd", "重置密码", "您的重置密码验证码是: %s, 5分钟内有效。"),

    /**
     * 绑定手机号
     */
    BIND_PHONE("bind_phone", "绑定手机号", "您的绑定手机号验证码是: %s, 5分钟内有效。"),

    /**
     * 验证绑定手机号
     */
    VERIFY_BIND("verify_bind", "验证绑定手机号", "您的验证码是: %s, 5分钟内有效。");

    /**
     * 场景代码
     */
    private final String code;

    /**
     * 场景名称
     */
    private final String name;

    /**
     * 短信模板
     */
    private final String template;

    SmsSceneEnum(String code, String name, String template) {
        this.code = code;
        this.name = name;
        this.template = template;
    }

    /**
     * 根据 code 获取枚举
     */
    public static SmsSceneEnum fromCode(String code) {
        for (SmsSceneEnum sceneEnum : values()) {
            if (sceneEnum.getCode().equals(code)) {
                return sceneEnum;
            }
        }
        throw new IllegalArgumentException("无效的短信场景: " + code);
    }

    /**
     * 格式化短信内容
     */
    public String formatSmsContent(String code) {
        return String.format(template, code);
    }
}
