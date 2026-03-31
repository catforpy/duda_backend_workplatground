package com.duda.user.dto.miniprogram;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小程序认证DTO
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
public class MiniProgramCertificationDTO implements Serializable {

    /**
     * 认证ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 小程序ID
     */
    private Long miniProgramId;

    /**
     * 认证状态
     */
    private String certificationStatus;

    /**
     * 认证时间
     */
    private LocalDateTime certificationTime;

    /**
     * 认证过期时间
     */
    private LocalDateTime certificationExpireTime;

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
