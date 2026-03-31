package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售商表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantSalesAgentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentCode;
    private String agentName;
    private String phone;
    private String referralCode;
    private BigDecimal commissionRate;
    private String status;
    private LocalDateTime createdAt;
}
