package com.duda.file.enums;

/**
 * 存储类型枚举（统一各云厂商的存储类型）
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public enum StorageClass {

    /**
     * 标准存储
     */
    STANDARD("standard", "标准存储"),

    /**
     * 低频访问存储
     */
    IA("ia", "低频访问"),

    /**
     * 归档存储
     */
    ARCHIVE("archive", "归档存储"),

    /**
     * 冷归档存储
     */
    COLD_ARCHIVE("cold-archive", "冷归档");

    private final String code;
    private final String name;

    StorageClass(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static StorageClass fromCode(String code) {
        for (StorageClass sc : values()) {
            if (sc.code.equals(code)) {
                return sc;
            }
        }
        throw new IllegalArgumentException("未知的存储类型: " + code);
    }
}
