package com.duda.user.enums;

import lombok.Getter;

/**
 * 用户类型枚举
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Getter
public enum UserTypeEnum {

    /**
     * 平台管理员
     */
    PLATFORM_ADMIN("platform_admin", "平台管理员"),

    /**
     * 服务商
     */
    SERVICE_PROVIDER("service_provider", "服务商"),

    /**
     * 都达网账户
     */
    PLATFORM_ACCOUNT("platform_account", "都达网账户"),

    /**
     * 后台管理员
     */
    BACKEND_ADMIN("backend_admin", "后台管理员");

    private final String code;
    private final String desc;

    UserTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static UserTypeEnum getByCode(String code) {
        for (UserTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
