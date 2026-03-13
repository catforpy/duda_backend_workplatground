package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感数据扫描记录实体
 * 对应sensitive_data_scan_record表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveDataScanRecord implements Serializable {

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
     * 扫描前缀
     */
    private String scanPrefix;

    /**
     * 状态: SCANNING, SUCCESS, FAILED
     */
    private String status;

    /**
     * 总文件数
     */
    private Long totalFiles;

    /**
     * 已扫描文件数
     */
    private Long scannedFiles;

    /**
     * 发现敏感文件数
     */
    private Long sensitiveFilesFound;

    /**
     * 总扫描大小(字节)
     */
    private Long totalScanSize;

    /**
     * 敏感数据类型统计(JSON)
     */
    private String sensitiveDataTypes;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 扫描耗时(秒)
     */
    private Long durationSeconds;

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
