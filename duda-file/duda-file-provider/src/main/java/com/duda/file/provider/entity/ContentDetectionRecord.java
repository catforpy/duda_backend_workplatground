package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 内容安全检测记录实体
 * 对应content_detection_record表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDetectionRecord implements Serializable {

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
     * 检测类型: IMAGE, AVATAR, AIGC
     */
    private String detectionType;

    /**
     * 状态: PROCESSING, SUCCESS, FAILED
     */
    private String status;

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

    /**
     * 风险等级: LOW, MEDIUM, HIGH
     */
    private String riskLevel;

    /**
     * 采取的操作: APPROVE, REJECT, REVIEW
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
