package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 服务商申请表PO
 *
 * 表名: company_service_providers
 * 说明: 公司服务商申请信息表
 * 租户隔离: 是（通过tenant_id字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("company_service_providers")
public class CompanyServiceProvidersPO extends BaseEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 租户ID（租户隔离字段）
     */
    private Long tenantId;

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
     * pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String status;

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
