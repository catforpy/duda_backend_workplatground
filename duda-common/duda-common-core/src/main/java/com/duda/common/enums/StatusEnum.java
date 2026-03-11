package com.duda.common.enums;

/**
 * 通用状态枚举
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public enum StatusEnum {

    /**
     * 启用/正常
     */
    ENABLE(1, "启用"),

    /**
     * 禁用/停用
     */
    DISABLE(0, "禁用"),

    /**
     * 删除
     */
    DELETED(-1, "已删除");

    private final Integer code;
    private final String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     */
    public static StatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 判断是否启用
     */
    public static boolean isEnabled(Integer code) {
        return ENABLE.getCode().equals(code);
    }

    /**
     * 判断是否禁用
     */
    public static boolean isDisabled(Integer code) {
        return DISABLE.getCode().equals(code);
    }

    /**
     * 判断是否删除
     */
    public static boolean isDeleted(Integer code) {
        return DELETED.getCode().equals(code);
    }
}
