package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分账规则表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantSplitRuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long cooperationId;
    private String ruleType;
    private Integer splitOrder;
    private Long receiverTenantId;
    private BigDecimal splitRatio;
    private String status;
    private LocalDateTime createdAt;
}
