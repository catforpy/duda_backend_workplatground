package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 病毒扫描记录DTO
 * 对应virus_scan_record表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirusScanRecordDTO implements Serializable {

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
     * 对象键
     */
    private String objectKey;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件版本ID
     */
    private String fileVersionId;

    /**
     * 状态：SCANNING/SUCCESS/FAILED
     */
    private String status;

    // ==================== 病毒检测结果 ====================

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
     * 扫描耗时（毫秒）
     */
    private Long scanTime;

    /**
     * 采取的操作：DELETE/QUARANTINE/MARK
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

    // ==================== 业务方法 ====================

    /**
     * 判断是否安全（无病毒）
     */
    public Boolean isSafe() {
        return "SUCCESS".equals(status) && !virusFound;
    }

    /**
     * 判断是否感染病毒
     */
    public Boolean isInfected() {
        return virusFound != null && virusFound;
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
     * 获取人类可读的扫描耗时
     */
    public String getHumanReadableScanTime() {
        if (scanTime == null) {
            return "unknown";
        }
        if (scanTime < 1000) {
            return scanTime + " ms";
        } else if (scanTime < 60000) {
            return String.format("%.2f s", scanTime / 1000.0);
        } else {
            return String.format("%.2f min", scanTime / 60000.0);
        }
    }
}
