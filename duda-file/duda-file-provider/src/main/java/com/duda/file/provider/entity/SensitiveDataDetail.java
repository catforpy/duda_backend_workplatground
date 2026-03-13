package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感数据详情实体
 * 对应sensitive_data_detail表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveDataDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 扫描任务ID
     */
    private String scanTaskId;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 敏感数据类型: PHONE, EMAIL, ID_CARD, BANK_CARD, PASSWORD, etc.
     */
    private String dataType;

    /**
     * 敏感等级: S1, S2, S3, S4
     */
    private String dataLevel;

    /**
     * 敏感数据数量
     */
    private Long dataCount;

    /**
     * 敏感数据样本(脱敏)
     */
    private String dataSample;

    /**
     * 出现位置(行号、列号等)(JSON)
     */
    private String occurrencePosition;

    /**
     * 风险等级: LOW, MEDIUM, HIGH, CRITICAL
     */
    private String riskLevel;

    /**
     * 采取的操作: NONE, MASK, ENCRYPT, DELETE
     */
    private String actionTaken;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
