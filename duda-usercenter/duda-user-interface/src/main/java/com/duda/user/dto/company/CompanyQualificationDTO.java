package com.duda.user.dto.company;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公司资质文件DTO
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
public class CompanyQualificationDTO implements Serializable {

    /**
     * 资质ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 资质类型
     */
    private String qualificationType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    /**
     * 乐观锁版本号
     */
    private Integer version;
    private LocalDateTime updateTime;
}
