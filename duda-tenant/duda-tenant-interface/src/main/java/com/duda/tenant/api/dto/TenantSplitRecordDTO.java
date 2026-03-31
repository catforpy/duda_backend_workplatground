package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分账记录表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantSplitRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long cooperationId;
    private Long orderId;
    private BigDecimal orderAmount;
    private BigDecimal splitAmount;
    private String status;
    private LocalDateTime createdAt;
}
