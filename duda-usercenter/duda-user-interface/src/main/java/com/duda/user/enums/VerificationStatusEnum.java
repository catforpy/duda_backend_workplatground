package com.duda.user.enums;

/**
 * API密钥验证状态枚举
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public enum VerificationStatusEnum {

    /**
     * 待验证
     */
    PENDING("pending", "待验证"),

    /**
     * 验证成功
     */
    SUCCESS("success", "验证成功"),

    /**
     * 验证失败
     */
    FAILED("failed", "验证失败");

    private final String code;
    private final String description;

    VerificationStatusEnum(String code, String description) {
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
    public static VerificationStatusEnum fromCode(String code) {
        for (VerificationStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown verification status: " + code);
    }
}
