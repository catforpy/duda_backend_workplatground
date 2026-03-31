package com.duda.user.dto.miniprogram;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小程序备案DTO
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
public class MiniProgramFilingDTO implements Serializable {

    /**
     * 备案ID
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
     * 备案状态
     */
    private String filingStatus;

    /**
     * 备案号
     */
    private String filingNo;

    /**
     * 备案时间
     */
    private LocalDateTime filingTime;

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
