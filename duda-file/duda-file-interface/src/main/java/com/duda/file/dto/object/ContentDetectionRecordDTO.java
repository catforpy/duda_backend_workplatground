package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 内容检测记录DTO
 * 对应content_detection_record表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDetectionRecordDTO implements Serializable {

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
     * 检测类型：IMAGE/AVATAR/AIGC
     */
    private String detectionType;

    /**
     * 状态：PROCESSING/SUCCESS/FAILED
     */
    private String status;

    // ==================== 色情检测 ====================

    /**
     * 色情标签
     */
    private String pornLabel;

    /**
     * 色情分数
     */
    private BigDecimal pornScore;

    /**
     * 色情置信度
     */
    private BigDecimal pornConfidence;

    // ==================== 政治检测 ====================

    /**
     * 政治标签
     */
    private String politicsLabel;

    /**
     * 政治分数
     */
    private BigDecimal politicsScore;

    /**
     * 政治置信度
     */
    private BigDecimal politicsConfidence;

    // ==================== 恐怖检测 ====================

    /**
     * 恐怖标签
     */
    private String terrorismLabel;

    /**
     * 恐怖分数
     */
    private BigDecimal terrorismScore;

    /**
     * 恐怖置信度
     */
    private BigDecimal terrorismConfidence;

    // ==================== 广告检测 ====================

    /**
     * 广告标签
     */
    private String adLabel;

    /**
     * 广告分数
     */
    private BigDecimal adScore;

    /**
     * 广告置信度
     */
    private BigDecimal adConfidence;

    // ==================== AIGC检测 ====================

    /**
     * 是否为AIGC生成
     */
    private Boolean isAigc;

    /**
     * AIGC置信度
     */
    private BigDecimal aigcConfidence;

    /**
     * AIGC工具类型
     */
    private String aigcToolType;

    // ==================== 风险评估 ====================

    /**
     * 风险等级：LOW/MEDIUM/HIGH
     */
    private String riskLevel;

    /**
     * 采取的操作：APPROVE/REJECT/REVIEW
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
     * 判断是否检测通过
     */
    public Boolean isPassed() {
        return "APPROVE".equals(actionTaken);
    }

    /**
     * 判断是否被拒绝
     */
    public Boolean isRejected() {
        return "REJECT".equals(actionTaken);
    }

    /**
     * 判断是否需要人工审核
     */
    public Boolean needReview() {
        return "REVIEW".equals(actionTaken);
    }
}
