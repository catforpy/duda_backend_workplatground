package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户配置DTO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值（JSON格式）
     */
    private String configValue;

    /**
     * 配置类型
     */
    private String configType;

    /**
     * 配置描述
     */
    private String configDesc;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
