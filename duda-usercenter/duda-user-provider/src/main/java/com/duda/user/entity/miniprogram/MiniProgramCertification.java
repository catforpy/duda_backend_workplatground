package com.duda.user.entity.miniprogram;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小程序认证Entity
 *
 * 对应数据库表：mini_program_certification
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@TableName("mini_program_certification")
public class MiniProgramCertification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
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
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

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
