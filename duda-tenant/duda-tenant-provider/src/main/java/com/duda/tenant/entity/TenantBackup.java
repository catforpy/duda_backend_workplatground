package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户备份实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_backups")
public class TenantBackup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 备份ID
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
     * 备份名称
     */
    private String backupName;

    /**
     * 备份类型（full/incremental）
     */
    private String backupType;

    /**
     * 备份文件路径
     */
    private String backupFilePath;

    /**
     * 备份文件大小（字节）
     */
    private Long backupFileSize;

    /**
     * 备份状态（pending/in_progress/success/failed）
     */
    private String backupStatus;

    /**
     * 备份开始时间
     */
    private LocalDateTime backupStartTime;

    /**
     * 备份结束时间
     */
    private LocalDateTime backupEndTime;

    /**
     * 备份耗时（秒）
     */
    private Long backupDuration;

    /**
     * 备份方式（manual/auto/scheduled）
     */
    private String backupMethod;

    /**
     * 备份描述
     */
    private String backupDesc;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 恢复次数
     */
    private Integer restoreCount;

    /**
     * 最后恢复时间
     */
    private LocalDateTime lastRestoreTime;

    /**
     * 过期时间（NULL表示永久保留）
     */
    private LocalDateTime expireTime;

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
