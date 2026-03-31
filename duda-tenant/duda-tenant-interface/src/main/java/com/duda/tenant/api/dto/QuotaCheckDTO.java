package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 配额检查结果DTO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class QuotaCheckDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否通过
     */
    private Boolean passed;

    /**
     * 配额类型
     */
    private String quotaType;

    /**
     * 当前值
     */
    private Long currentValue;

    /**
     * 最大值
     */
    private Long maxValue;

    /**
     * 使用率（百分比）
     */
    private Double usageRate;

    /**
     * 错误信息
     */
    private String errorMessage;
}
