package com.duda.common.enums;

/**
 * 用户类型枚举
 *
 * 定义4种用户身份
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public enum UserType {

    /**
     * 普通用户
     * - 可以浏览商品、下单购买
     * - 可以发布社交动态
     * - 可以观看直播
     */
    NORMAL("normal", "普通用户", 1),

    /**
     * 商家用户
     * - 可以发布商品和管理店铺
     * - 可以进行直播带货
     * - 有完整的电商后台管理权限
     */
    MERCHANT("merchant", "商家用户", 2),

    /**
     * 运营人员
     * - 可以管理平台内容和用户
     * - 可以查看运营数据
     * - 不能进行系统配置
     */
    OPERATOR("operator", "运营人员", 3),

    /**
     * 管理员
     * - 拥有所有权限
     * - 可以进行系统配置
     * - 可以管理所有用户
     */
    ADMIN("admin", "管理员", 4);

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 类型级别（数字越大权限越高）
     */
    private final Integer level;

    UserType(String code, String name, Integer level) {
        this.code = code;
        this.name = name;
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getLevel() {
        return level;
    }

    /**
     * 根据代码获取枚举
     */
    public static UserType fromCode(String code) {
        for (UserType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的用户类型: " + code);
    }

    /**
     * 根据级别获取枚举
     */
    public static UserType fromLevel(Integer level) {
        for (UserType type : values()) {
            if (type.getLevel().equals(level)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的用户级别: " + level);
    }

    /**
     * 检查是否有足够权限（当前级别 >= 目标级别）
     */
    public boolean hasPermission(UserType targetType) {
        return this.level >= targetType.level;
    }
}
