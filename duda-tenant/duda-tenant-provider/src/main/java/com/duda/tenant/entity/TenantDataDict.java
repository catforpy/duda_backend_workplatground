package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户数据字典实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_data_dict")
public class TenantDataDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID（NULL表示全局字典）
     */
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 字典编码（全局唯一）
     */
    private String dictCode;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典值（JSON格式）
     */
    private String dictValue;

    /**
     * 字典描述
     */
    private String dictDesc;

    /**
     * 父字典ID
     */
    private Long parentId;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 是否系统字典
     */
    private Integer isSystem;

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
