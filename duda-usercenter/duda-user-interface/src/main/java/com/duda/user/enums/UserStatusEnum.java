package com.duda.user.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Getter
public enum UserStatusEnum {

    /**
     * 激活
     */
    ACTIVE("active", "激活"),

    /**
     * 未激活
     */
    INACTIVE("inactive", "未激活"),

    /**
     * 冻结
     */
    FROZEN("frozen", "冻结"),

    /**
     * 已删除
     */
    DELETED("deleted", "已删除");

    private final String code;
    private final String desc;

    UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static UserStatusEnum getByCode(String code) {
        for (UserStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
