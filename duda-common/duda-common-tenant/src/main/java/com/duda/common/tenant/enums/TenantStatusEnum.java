package com.duda.common.tenant.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 租户状态枚举
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Getter
public enum TenantStatusEnum {

    /**
     * 激活
     */
    ACTIVE("active", "激活"),

    /**
     * 暂停
     */
    SUSPENDED("suspended", "暂停"),

    /**
     * 冻结
     */
    FROZEN("frozen", "冻结"),

    /**
     * 过期
     */
    EXPIRED("expired", "过期"),

    /**
     * 已删除
     */
    DELETED("deleted", "已删除");

    @EnumValue
    private final String code;

    @JsonValue
    private final String displayName;

    TenantStatusEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static TenantStatusEnum fromCode(String code) {
        for (TenantStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("Unknown tenant status: " + code);
    }
}
