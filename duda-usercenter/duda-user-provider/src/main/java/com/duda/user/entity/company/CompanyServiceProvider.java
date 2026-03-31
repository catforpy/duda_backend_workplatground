package com.duda.user.entity.company;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务商申请Entity
 *
 * 对应数据库表：company_service_providers
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@TableName("company_service_providers")
public class CompanyServiceProvider implements Serializable {

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
