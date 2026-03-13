package com.duda.file.enums;

/**
 * 权限类型枚举（统一各云厂商的ACL定义）
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public enum AclType {

    /**
     * 私有
     */
    PRIVATE("private", "私有"),

    /**
     * 公共读
     */
    PUBLIC_READ("public-read", "公共读"),

    /**
     * 公共读写
     */
    PUBLIC_READ_WRITE("public-read-write", "公共读写");

    private final String code;
    private final String name;

    AclType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AclType fromCode(String code) {
        for (AclType acl : values()) {
            if (acl.code.equals(code)) {
                return acl;
            }
        }
        throw new IllegalArgumentException("未知的权限类型: " + code);
    }
}
