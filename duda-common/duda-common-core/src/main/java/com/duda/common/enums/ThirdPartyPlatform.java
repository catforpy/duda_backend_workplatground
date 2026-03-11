package com.duda.common.enums;

/**
 * 第三方平台枚举
 *
 * 定义支持的第三方登录平台
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public enum ThirdPartyPlatform {

    /**
     * 微信
     * - 支持微信开放平台
     * - 支持微信小程序
     */
    WECHAT("wechat", "微信", "https://open.weixin.qq.com"),

    /**
     * QQ
     * - QQ互联
     */
    QQ("qq", "QQ", "https://connect.qq.com"),

    /**
     * 支付宝
     * - 支付宝授权登录
     */
    ALIPAY("alipay", "支付宝", "https://open.alipay.com"),

    /**
     * 微博
     * - 微博开放平台
     */
    WEIBO("weibo", "微博", "https://open.weibo.com"),

    /**
     * 抖音
     * - 抖音开放平台
     */
    DOUYIN("douyin", "抖音", "https://developer.open-douyin.com"),

    /**
     * Apple
     * - Sign in with Apple
     */
    APPLE("apple", "Apple", "https://developer.apple.com");

    /**
     * 平台代码
     */
    private final String code;

    /**
     * 平台名称
     */
    private final String name;

    /**
     * 开放平台地址
     */
    private final String platformUrl;

    ThirdPartyPlatform(String code, String name, String platformUrl) {
        this.code = code;
        this.name = name;
        this.platformUrl = platformUrl;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPlatformUrl() {
        return platformUrl;
    }

    /**
     * 根据代码获取枚举
     */
    public static ThirdPartyPlatform fromCode(String code) {
        for (ThirdPartyPlatform platform : values()) {
            if (platform.getCode().equals(code)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("不支持的第三方平台: " + code);
    }
}
