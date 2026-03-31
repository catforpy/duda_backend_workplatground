package com.duda.common.tenant.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Getter
public enum OrderStatusEnum {

    /**
     * 未支付
     */
    UNPAID("unpaid", "未支付"),

    /**
     * 已支付
     */
    PAID("paid", "已支付"),

    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消"),

    /**
     * 已退款
     */
    REFUNDED("refunded", "已退款"),

    /**
     * 部分退款
     */
    PARTIAL_REFUND("partial_refund", "部分退款");

    @EnumValue
    private final String code;

    @JsonValue
    private final String displayName;

    OrderStatusEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static OrderStatusEnum fromCode(String code) {
        for (OrderStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + code);
    }
}
