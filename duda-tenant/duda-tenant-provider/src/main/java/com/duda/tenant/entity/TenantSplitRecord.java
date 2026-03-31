package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分账记录表实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("tenant_split_records")
public class TenantSplitRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cooperationId;
    private Long orderId;
    private BigDecimal orderAmount;
    private BigDecimal splitAmount;
    private String status;
    private LocalDateTime createdAt;
}
