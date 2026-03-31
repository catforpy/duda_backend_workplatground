package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感数据扫描记录DTO
 * 对应sensitive_data_scan_record表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveDataScanRecordDTO implements Serializable {

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
     * Bucket名称
     */
    private String bucketName;

    /**
     * 扫描前缀
     */
    private String scanPrefix;

    /**
     * 状态：SCANNING/SUCCESS/FAILED
     */
    private String status;

    // ==================== 扫描统计 ====================

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
     * 总扫描大小（字节）
     */
    private Long totalScanSize;

    /**
     * 敏感数据类型统计（JSON格式）
     */
    private String sensitiveDataTypes;

    // ==================== 时间统计 ====================

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 扫描耗时（秒）
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

    // ==================== 业务方法 ====================

    /**
     * 获取扫描进度百分比
     */
    public Double getProgressPercentage() {
        if (totalFiles == null || totalFiles == 0) {
            return 0.0;
        }
        if (scannedFiles == null) {
            return 0.0;
        }
        return (scannedFiles * 100.0) / totalFiles;
    }

    /**
     * 判断扫描是否完成
     */
    public Boolean isCompleted() {
        return "SUCCESS".equals(status) || "FAILED".equals(status);
    }

    /**
     * 判断是否正在扫描
     */
    public Boolean isScanning() {
        return "SCANNING".equals(status);
    }

    /**
     * 判断是否发现敏感数据
     */
    public Boolean hasSensitiveData() {
        return sensitiveFilesFound != null && sensitiveFilesFound > 0;
    }

    /**
     * 获取人类可读的扫描耗时
     */
    public String getHumanReadableDuration() {
        if (durationSeconds == null) {
            return "unknown";
        }
        if (durationSeconds < 60) {
            return durationSeconds + " seconds";
        } else if (durationSeconds < 3600) {
            return String.format("%.2f minutes", durationSeconds / 60.0);
        } else {
            return String.format("%.2f hours", durationSeconds / 3600.0);
        }
    }
}
