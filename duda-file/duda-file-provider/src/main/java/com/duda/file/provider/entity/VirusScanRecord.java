package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 病毒扫描记录实体
 * 对应virus_scan_record表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirusScanRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件版本ID
     */
    private String fileVersionId;

    /**
     * 状态: SCANNING, SUCCESS, FAILED
     */
    private String status;

    /**
     * 是否发现病毒
     */
    private Boolean virusFound;

    /**
     * 病毒类型
     */
    private String virusType;

    /**
     * 病毒名称
     */
    private String virusName;

    /**
     * 扫描耗时(毫秒)
     */
    private Long scanTime;

    /**
     * 采取的操作: DELETE, QUARANTINE, MARK
     */
    private String actionTaken;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
