package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 小程序微信认证表PO
 *
 * 表名: mini_program_certification
 * 说明: 小程序微信认证信息表
 * 租户隔离: 是（通过tenant_id字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mini_program_certification")
public class MiniProgramCertificationPO extends BaseEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 租户ID（租户隔离字段）
     */
    private Long tenantId;

    /**
     * 小程序ID（关联mini_programs.id）
     */
    private Long miniProgramId;

    /**
     * 认证状态
     * none-未认证, pending-认证中, certified-已认证, expired-已过期
     */
    private String certificationStatus;

    /**
     * 认证时间
     */
    private Date certificationTime;

    /**
     * 认证过期时间
     */
    private Date certificationExpireTime;

    /**
     * 删除标记
     * 0-正常, 1-已删除
     */
    private Byte deleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
