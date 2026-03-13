package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 安全配置实体
 * 对应security_config表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 加密类型: SSE_OSS, SSE_KMS
     */
    private String encryptionType;

    /**
     * KMS密钥ID
     */
    private String kmsKeyId;

    /**
     * 是否启用服务端加密
     */
    private Boolean enableEncryption;

    /**
     * 是否启用客户端加密
     */
    private Boolean enableClientEncryption;

    /**
     * TLS版本: TLSv1.0, TLSv1.1, TLSv1.2, TLSv1.3
     */
    private String tlsVersion;

    /**
     * 完整性校验类型: NONE, MD5, CRC64, BOTH
     */
    private String integrityCheckType;

    /**
     * 是否启用内容安全检测
     */
    private Boolean enableContentDetection;

    /**
     * 是否启用病毒检测
     */
    private Boolean enableVirusScan;

    /**
     * 是否启用敏感数据扫描
     */
    private Boolean enableSensitiveDataScan;

    /**
     * 内容安全检测阈值(0-100)
     */
    private BigDecimal contentDetectionThreshold;

    /**
     * 病毒检测后操作: DELETE, QUARANTINE, MARK
     */
    private String virusScanAction;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
