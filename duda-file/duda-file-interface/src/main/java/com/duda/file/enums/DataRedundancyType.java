package com.duda.file.enums;

/**
 * 数据冗余类型枚举
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public enum DataRedundancyType {

    /**
     * 本地冗余存储
     * 成本更低
     * 单可用区
     * 适用于非核心或测试数据
     */
    LRS("lrs", "本地冗余存储"),

    /**
     * 同城冗余存储
     * 生产环境推荐
     * 多可用区（AZ）
     * 99.9999999999%（12个9）的数据持久性
     */
    ZRS("zrs", "同城冗余存储");

    private final String code;
    private final String name;

    DataRedundancyType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static DataRedundancyType fromCode(String code) {
        for (DataRedundancyType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的数据冗余类型: " + code);
    }
}
