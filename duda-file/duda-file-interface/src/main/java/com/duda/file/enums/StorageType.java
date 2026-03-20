package com.duda.file.enums;

/**
 * 存储类型枚举
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public enum StorageType {

    /**
     * 阿里云OSS
     */
    ALIYUN_OSS("OSS", "阿里云OSS"),

    /**
     * 腾讯云COS
     */
    TENCENT_COS("tencent-cos", "腾讯云COS"),

    /**
     * 千牛云Kodo
     */
    QINIU_KODO("qiniu-kodo", "千牛云Kodo"),

    /**
     * 亚马逊S3
     */
    AWS_S3("aws-s3", "亚马逊S3"),

    /**
     * MinIO
     */
    MINIO("minio", "MinIO");

    private final String code;
    private final String name;

    StorageType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据代码获取存储类型
     */
    public static StorageType fromCode(String code) {
        for (StorageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的存储类型: " + code);
    }
}
