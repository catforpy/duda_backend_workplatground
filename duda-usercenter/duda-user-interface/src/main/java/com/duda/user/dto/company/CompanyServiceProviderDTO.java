package com.duda.user.dto.company;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务商申请DTO
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
public class CompanyServiceProviderDTO implements Serializable {

    /**
     * 服务商ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 申请类型
     */
    private String applyType;

    /**
     * 目标级别
     */
    private String targetLevel;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
