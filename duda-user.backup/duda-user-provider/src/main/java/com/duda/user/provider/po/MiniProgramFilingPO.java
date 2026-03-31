package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 小程序备案表PO
 *
 * 表名: mini_program_filing
 * 说明: 小程序备案信息表
 * 租户隔离: 是（通过tenant_id字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mini_program_filing")
public class MiniProgramFilingPO extends BaseEntity {

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
     * 备案状态
     * none-未备案, pending-备案中, filed-已备案
     */
    private String filingStatus;

    /**
     * 备案号
     */
    private String filingNo;

    /**
     * 备案时间
     */
    private Date filingTime;

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
