package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户配置变更历史实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_config_history")
public class TenantConfigHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 变更前值
     */
    private String oldValue;

    /**
     * 变更后值
     */
    private String newValue;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 扩展字段（JSON格式）
     */
    private String extendFields;

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
