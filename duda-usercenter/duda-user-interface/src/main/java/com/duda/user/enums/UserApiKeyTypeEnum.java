package com.duda.user.enums;

/**
 * 用户API密钥类型枚举
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public enum UserApiKeyTypeEnum {

    /**
     * 阿里云OSS
     */
    ALIYUN_OSS("aliyun_oss", "阿里云OSS"),

    /**
     * 腾讯云COS
     */
    TENCENT_COS("tencent_cos", "腾讯云COS"),

    /**
     * 七牛云Kodo
     */
    QINIU_KODO("qiniu_kodo", "七牛云Kodo"),

    /**
     * MinIO
     */
    MINIO("minio", "MinIO");

    private final String code;
    private final String description;

    UserApiKeyTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static UserApiKeyTypeEnum fromCode(String code) {
        for (UserApiKeyTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown API key type: " + code);
    }
}
