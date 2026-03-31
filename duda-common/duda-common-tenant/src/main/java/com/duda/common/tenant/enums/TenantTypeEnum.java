package com.duda.common.tenant.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 租户类型枚举
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Getter
public enum TenantTypeEnum {

    /**
     * 试用版
     */
    TRIAL("trial", "试用版"),

    /**
     * 基础版
     */
    BASIC("basic", "基础版"),

    /**
     * 专业版
     */
    PROFESSIONAL("professional", "专业版"),

    /**
     * 企业版
     */
    ENTERPRISE("enterprise", "企业版");

    /**
     * 类型代码（存储在数据库）
     */
    @EnumValue
    private final String code;

    /**
     * 类型名称（显示用）
     */
    @JsonValue
    private final String displayName;

    TenantTypeEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * 根据code获取枚举
     */
    public static TenantTypeEnum fromCode(String code) {
        for (TenantTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("Unknown tenant type: " + code);
    }
}
